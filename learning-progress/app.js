import {
  ENDPOINT_KEY,
  SECRET_KEY,
  chooseInitialProgress,
  createProgressClient,
  loadLocalProgress,
  normalizeEndpoint,
  normalizeProgress,
  saveLocalProgress,
} from "./cloud-sync.mjs";

const course = window.COURSE_PROGRESS_DATA;
const planRoot = document.querySelector("#plan");
if (!course?.videos?.length) {
  planRoot.innerHTML = '<div class="empty">课程任务数据加载失败，请先运行生成脚本。</div>';
  throw new Error("Course progress data is unavailable");
}

const tasks = course.videos;
const validTaskIds = new Set(tasks.map((task) => task.id));
const initialLocal = loadLocalProgress(localStorage, validTaskIds);
const completed = new Set(initialLocal.state.completed);
let completedAt = { ...(initialLocal.state.completedAt || {}) };
let localState = initialLocal.state;
let hasLocalState = initialLocal.exists;
let localRevision = hasLocalState ? 1 : 0;
let syncedRevision = 0;
let activeFilter = "all";
let searchQuery = "";
let selectedHeatmapDay = null;
let endpoint = localStorage.getItem(ENDPOINT_KEY) || "";
const legacySessionSecret = sessionStorage.getItem(SECRET_KEY);
if (!localStorage.getItem(SECRET_KEY) && legacySessionSecret) {
  localStorage.setItem(SECRET_KEY, legacySessionSecret);
  sessionStorage.removeItem(SECRET_KEY);
}
let syncSecret = localStorage.getItem(SECRET_KEY) || "";
let syncClient = null;
let syncTimer = null;
let syncInFlight = false;

const syncButton = document.querySelector("#syncButton");
const syncStatus = document.querySelector("#syncStatus");
const syncDialog = document.querySelector("#syncDialog");
const syncForm = document.querySelector("#syncForm");
const workerUrlInput = document.querySelector("#workerUrl");
const syncSecretInput = document.querySelector("#syncSecret");
const syncError = document.querySelector("#syncError");

const escapeHtml = (value) => String(value)
  .replaceAll("&", "&amp;")
  .replaceAll("<", "&lt;")
  .replaceAll(">", "&gt;")
  .replaceAll('"', "&quot;")
  .replaceAll("'", "&#039;");

const displayDate = (date) => `${String(date.getMonth() + 1).padStart(2, "0")}.${String(date.getDate()).padStart(2, "0")}`;
const isoDate = (date) => `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}-${String(date.getDate()).padStart(2, "0")}`;
const todayIso = () => isoDate(new Date());
const ACTIVITY_START_KEY = "full-stack-ai-activity-start";
const activityStartDate = (() => {
  const today = todayIso();
  const saved = localStorage.getItem(ACTIVITY_START_KEY);
  if (saved && /^\d{4}-\d{2}-\d{2}$/.test(saved) && saved <= today) return saved;
  localStorage.setItem(ACTIVITY_START_KEY, today);
  return today;
})();
const formatDuration = (seconds) => {
  const hours = Math.floor(seconds / 3600);
  const minutes = Math.floor((seconds % 3600) / 60);
  const remainingSeconds = seconds % 60;
  return hours ? `${hours}h ${String(minutes).padStart(2, "0")}m` : `${minutes}:${String(remainingSeconds).padStart(2, "0")}`;
};

const weekGroups = [...new Set(tasks.map((task) => task.week))].map((weekNumber) => {
  const weekTasks = tasks.filter((task) => task.week === weekNumber);
  return {
    number: weekNumber,
    title: weekTasks[0].weekTitle,
    tasks: weekTasks,
  };
});

function setSyncStatus(message, state = "idle") {
  syncStatus.textContent = message;
  syncButton.dataset.state = state;
}

function persistCurrent(updatedAt = new Date().toISOString(), markChanged = true) {
  localState = saveLocalProgress(localStorage, {
    version: 2,
    updatedAt,
    completed: [...completed],
    completedAt,
  }, validTaskIds);
  hasLocalState = true;
  if (markChanged) localRevision += 1;
}

function replaceProgress(state) {
  completed.clear();
  state.completed.forEach((id) => completed.add(id));
  completedAt = { ...(state.completedAt || {}) };
  localState = saveLocalProgress(localStorage, state, validTaskIds);
  hasLocalState = true;
  localRevision += 1;
  syncedRevision = localRevision;
  render();
}

function matches(task) {
  const done = completed.has(task.id);
  if (activeFilter === "open" && done) return false;
  if (activeFilter === "done" && !done) return false;
  if (activeFilter.startsWith("date:") && completedAt[task.id] !== activeFilter.slice(5)) return false;
  if (!searchQuery) return true;
  const haystack = `${task.title} ${task.chapterTitle} ${task.weekTitle}`.toLowerCase();
  return haystack.includes(searchQuery);
}

function taskMarkup(task) {
  const fileUrl = encodeURI(`file://${task.absolutePath}`);
  return `
    <article class="task${completed.has(task.id) ? " is-complete" : ""}" data-task-id="${task.id}">
      <div class="task-context"><span>CH ${String(task.chapter).padStart(2, "0")}</span><small>${escapeHtml(task.chapterTitle)}</small></div>
      <input class="check" type="checkbox" aria-label="完成：${escapeHtml(task.title)}" ${completed.has(task.id) ? "checked" : ""} />
      <div>
        <h4 class="task-title"><a class="video-link" href="${fileUrl}" title="打开本地视频">${escapeHtml(task.title)}</a></h4>
        <p class="task-path">${escapeHtml(task.relativePath)}</p>
      </div>
      <div class="video-meta">${formatDuration(task.durationSeconds)}<br>视频 ${tasks.indexOf(task) + 1} / ${tasks.length}</div>
    </article>
  `;
}

function setCollapseState(button, expanded) {
  button.setAttribute("aria-expanded", String(expanded));
  button.classList.toggle("is-collapsed", !expanded);
  const label = button.querySelector(".collapse-label");
  if (label) label.textContent = expanded ? "收起" : "展开";
}

function updateOverview() {
  const count = completed.size;
  const percent = Math.round((count / tasks.length) * 100);
  const remainingSeconds = tasks.filter((task) => !completed.has(task.id)).reduce((sum, task) => sum + task.durationSeconds, 0);
  document.querySelector("#percentage").textContent = `${percent}%`;
  document.querySelector("#progressText").textContent = `已完成 ${count} / ${tasks.length} 个视频`;
  document.querySelector("#remainingText").textContent = count === tasks.length ? "全部完成" : `剩余约 ${formatDuration(remainingSeconds)}`;
  document.querySelector("#progressFill").style.width = `${percent}%`;
  document.querySelector("[role=progressbar]").setAttribute("aria-valuenow", String(percent));
  const firstOpen = tasks.find((task) => !completed.has(task.id));
  document.querySelector("#todayAdvice").textContent = firstOpen ? "继续下一项" : "全部完成，准备面试";
  renderHeatmap();
}

function renderHeatmap() {
  const heatmap = document.querySelector("#heatmap");
  const today = new Date();
  const start = new Date(`${activityStartDate}T00:00:00`);
  const daysSinceStart = Math.max(0, Math.floor((today - start) / 86400000));
  const dates = Array.from({ length: daysSinceStart + 1 }, (_, index) => {
    const date = new Date(start);
    date.setDate(date.getDate() + index);
    return date;
  });
  const counts = dates.map((date) => tasks.filter((task) => completedAt[task.id] === isoDate(date)).length);
  const maxCount = Math.max(1, ...counts);
  const cells = dates.map((date, index) => {
    const dateKey = isoDate(date);
    const doneCount = counts[index];
    const level = doneCount === 0 ? 0 : doneCount >= maxCount ? 4 : doneCount >= maxCount * .66 ? 3 : doneCount >= maxCount * .33 ? 2 : 1;
    const selected = selectedHeatmapDay === dateKey || activeFilter === `date:${dateKey}`;
    const isToday = dateKey === todayIso();
    return `<button class="heat-cell${isToday ? " is-today" : ""}${selected ? " is-selected" : ""}" data-level="${level}" data-date="${dateKey}" type="button" role="listitem" title="${dateKey} · 学习 ${doneCount} 个视频" aria-label="${dateKey}，学习 ${doneCount} 个视频"><span class="heat-date">${displayDate(date)}</span><span class="heat-count">${doneCount || "—"}</span></button>`;
  });
  heatmap.innerHTML = cells.join("");
  heatmap.querySelectorAll(".heat-cell").forEach((cell) => {
    cell.addEventListener("click", () => {
      const date = cell.dataset.date;
      const isSelected = selectedHeatmapDay === date;
      selectedHeatmapDay = isSelected ? null : date;
      activeFilter = isSelected ? "all" : `date:${date}`;
      document.querySelectorAll(".filter").forEach((item) => item.setAttribute("aria-pressed", String(isSelected && item.dataset.filter === "all")));
      render();
      if (!isSelected) document.querySelector("#plan").scrollIntoView({ behavior: "smooth", block: "start" });
    });
  });
  const activeDays = new Set(Object.values(completedAt).filter((date) => date >= activityStartDate)).size;
  const todayDone = tasks.filter((task) => completedAt[task.id] === todayIso()).length;
  document.querySelector("#heatmapSummary").textContent = `${activeDays} 天有学习记录 · 今日 ${todayDone} 个视频`;
}

function render() {
  planRoot.innerHTML = "";
  let visibleTaskCount = 0;

  for (const week of weekGroups) {
    const visibleWeekTasks = week.tasks.filter(matches);
    if (!visibleWeekTasks.length) continue;
    visibleTaskCount += visibleWeekTasks.length;
    const doneInWeek = week.tasks.filter((task) => completed.has(task.id)).length;
    const weekHours = week.tasks.reduce((sum, task) => sum + task.durationSeconds, 0);
    const section = document.createElement("section");
    section.className = "week";
    section.innerHTML = `
      <button class="week-heading" type="button" aria-expanded="true">
        <span class="week-number">Week ${String(week.number).padStart(2, "0")}</span>
        <h2 class="week-title">${escapeHtml(week.title)}</h2>
        <span class="week-count">${doneInWeek} / ${week.tasks.length} · ${formatDuration(weekHours)}</span>
        <span class="collapse-indicator" aria-hidden="true"><span class="collapse-label">收起</span><span class="collapse-chevron"></span></span>
      </button>
      <div class="week-items"></div>
    `;

    const weekItems = section.querySelector(".week-items");
    weekItems.innerHTML = visibleWeekTasks.map(taskMarkup).join("");

    const weekButton = section.querySelector(".week-heading");
    weekButton.addEventListener("click", () => {
      const expanded = weekButton.getAttribute("aria-expanded") === "true";
      setCollapseState(weekButton, !expanded);
      section.classList.toggle("is-collapsed", expanded);
      weekItems.hidden = expanded;
    });
    planRoot.append(section);
  }

  if (!visibleTaskCount) planRoot.innerHTML = '<div class="empty">这个筛选条件下没有视频。</div>';
  planRoot.querySelectorAll(".check").forEach((checkbox) => {
    checkbox.addEventListener("change", (event) => {
      const taskId = event.target.closest(".task").dataset.taskId;
      if (event.target.checked) {
        completed.add(taskId);
        completedAt[taskId] = todayIso();
      } else {
        completed.delete(taskId);
        delete completedAt[taskId];
      }
      persistCurrent();
      render();
      scheduleCloudSync();
    });
  });
  updateOverview();
}

function syncErrorMessage(error) {
  if (!navigator.onLine) return "当前离线，进度已保存在本机";
  if (error?.status === 401) return "同步码不正确，请重新设置";
  if (error?.status === 503) return "请先在 Cloudflare 添加 SYNC_SECRET";
  return error?.message || "云同步失败，进度仍保存在本机";
}

async function uploadSnapshot(snapshot) {
  const saved = normalizeProgress(await syncClient.put(snapshot, completedAt), validTaskIds);
  if (!saved.updatedAt) throw new Error("云端返回的数据缺少更新时间");
  return saved;
}

async function flushCloudSync() {
  if (!syncClient || syncInFlight || syncedRevision >= localRevision) return;
  syncInFlight = true;

  try {
    while (syncedRevision < localRevision) {
      const targetRevision = localRevision;
      const snapshot = [...completed];
      setSyncStatus("正在同步…", "syncing");
      const saved = await uploadSnapshot(snapshot);
      syncedRevision = targetRevision;
      if (targetRevision === localRevision) {
        localState = saveLocalProgress(localStorage, {
          ...saved,
          completed: snapshot,
          completedAt,
        }, validTaskIds);
        setSyncStatus(`已同步 ${new Date(saved.updatedAt).toLocaleTimeString("zh-CN", { hour: "2-digit", minute: "2-digit" })}`, "synced");
      }
    }
  } catch (error) {
    setSyncStatus(syncErrorMessage(error), "error");
  } finally {
    syncInFlight = false;
  }
}

function scheduleCloudSync() {
  if (!endpoint) {
    setSyncStatus("仅本地 · 设置同步", "idle");
    return;
  }
  if (!syncSecret) {
    setSyncStatus("云端未连接 · 输入同步码", "pending");
    return;
  }
  setSyncStatus("待同步", "pending");
  window.clearTimeout(syncTimer);
  syncTimer = window.setTimeout(flushCloudSync, 700);
}

async function initializeCloudSync() {
  if (!endpoint) {
    syncClient = null;
    setSyncStatus("仅本地 · 设置同步", "idle");
    return;
  }
  if (!syncSecret) {
    syncClient = null;
    setSyncStatus("云端未连接 · 输入同步码", "pending");
    return;
  }

  try {
    syncClient = createProgressClient({ endpoint, secret: syncSecret });
    setSyncStatus("正在读取云端…", "syncing");
    const remote = normalizeProgress(await syncClient.get(), validTaskIds);
    const decision = chooseInitialProgress({ exists: hasLocalState, state: localState }, remote);

    if (decision === "use-remote") {
      replaceProgress(remote);
      setSyncStatus("已读取云端进度", "synced");
      return;
    }

    if (decision === "upload-local") {
      const snapshot = [...completed];
      const saved = await uploadSnapshot(snapshot);
      localState = saveLocalProgress(localStorage, { ...saved, completed: snapshot, completedAt }, validTaskIds);
      syncedRevision = localRevision;
      setSyncStatus("本机进度已同步", "synced");
      return;
    }

    syncedRevision = localRevision;
    setSyncStatus("云端已连接", "synced");
  } catch (error) {
    syncClient = null;
    setSyncStatus(syncErrorMessage(error), "error");
  }
}

document.querySelectorAll(".filter").forEach((button) => {
  button.addEventListener("click", () => {
    activeFilter = button.dataset.filter;
    document.querySelectorAll(".filter").forEach((item) => item.setAttribute("aria-pressed", String(item === button)));
    render();
  });
});

document.querySelector("#searchInput").addEventListener("input", (event) => {
  searchQuery = event.target.value.trim().toLowerCase();
  render();
});

document.querySelector("#resetButton").addEventListener("click", () => {
  if (!completed.size || !window.confirm(`确定清空全部 ${tasks.length} 个视频的学习进度吗？此操作会同步到云端。`)) return;
  completed.clear();
  completedAt = {};
  persistCurrent();
  render();
  scheduleCloudSync();
});

syncButton.addEventListener("click", () => {
  workerUrlInput.value = endpoint;
  syncSecretInput.value = "";
  syncSecretInput.placeholder = syncSecret ? "本浏览器已保存；留空则不修改" : "填写你在 Cloudflare 设置的同步码";
  syncError.textContent = "";
  syncDialog.showModal();
});

document.querySelector("#closeSyncDialog").addEventListener("click", () => syncDialog.close());
document.querySelector("#cancelSyncDialog").addEventListener("click", () => syncDialog.close());

syncForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  syncError.textContent = "";
  try {
    endpoint = normalizeEndpoint(workerUrlInput.value);
    syncSecret = syncSecretInput.value.trim() || syncSecret;
    if (!syncSecret) throw new TypeError("请填写同步码");
    localStorage.setItem(ENDPOINT_KEY, endpoint);
    localStorage.setItem(SECRET_KEY, syncSecret);
    syncDialog.close();
    await initializeCloudSync();
  } catch (error) {
    syncError.textContent = error.message;
  }
});

document.querySelector("#disconnectSync").addEventListener("click", () => {
  localStorage.removeItem(ENDPOINT_KEY);
  localStorage.removeItem(SECRET_KEY);
  endpoint = "";
  syncSecret = "";
  syncClient = null;
  syncDialog.close();
  setSyncStatus("仅本地 · 设置同步", "idle");
});

const today = new Date();
document.querySelector("#deadlineDate").textContent = `${String(today.getMonth() + 1).padStart(2, "0")} 月 ${String(today.getDate()).padStart(2, "0")} 日`;
document.querySelector("#planMeta").textContent = `${tasks.length} 个视频 · ${formatDuration(course.totalDurationSeconds)} · 来自真实课程目录`;
render();
initializeCloudSync();
