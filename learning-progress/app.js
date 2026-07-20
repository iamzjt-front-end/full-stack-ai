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
let localState = initialLocal.state;
let hasLocalState = initialLocal.exists;
let localRevision = hasLocalState ? 1 : 0;
let syncedRevision = 0;
let activeFilter = "all";
let searchQuery = "";
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

const startDate = new Date(`${course.startDate}T00:00:00+08:00`);
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

const dateForDay = (day) => {
  const date = new Date(startDate);
  date.setDate(date.getDate() + day - 1);
  return date;
};

const displayDate = (date) => `${String(date.getMonth() + 1).padStart(2, "0")}.${String(date.getDate()).padStart(2, "0")}`;
const isoDate = (date) => `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}-${String(date.getDate()).padStart(2, "0")}`;
const formatDuration = (seconds) => {
  const hours = Math.floor(seconds / 3600);
  const minutes = Math.floor((seconds % 3600) / 60);
  const remainingSeconds = seconds % 60;
  return hours ? `${hours}h ${String(minutes).padStart(2, "0")}m` : `${minutes}:${String(remainingSeconds).padStart(2, "0")}`;
};

const currentPlanDay = () => {
  const today = new Date();
  const localToday = new Date(today.getFullYear(), today.getMonth(), today.getDate());
  const localStart = new Date(startDate.getFullYear(), startDate.getMonth(), startDate.getDate());
  const elapsedDays = Math.floor((localToday - localStart) / 86400000) + 1;
  return Math.min(course.targetDays, Math.max(1, elapsedDays));
};

const weekGroups = [...new Set(tasks.map((task) => task.week))].map((weekNumber) => {
  const weekTasks = tasks.filter((task) => task.week === weekNumber);
  const chapterGroups = [...new Set(weekTasks.map((task) => task.chapter))].map((chapterNumber) => ({
    number: chapterNumber,
    title: weekTasks.find((task) => task.chapter === chapterNumber).chapterTitle,
    tasks: weekTasks.filter((task) => task.chapter === chapterNumber),
  }));
  return {
    number: weekNumber,
    title: weekTasks[0].weekTitle,
    tasks: weekTasks,
    chapters: chapterGroups,
  };
});

function setSyncStatus(message, state = "idle") {
  syncStatus.textContent = message;
  syncButton.dataset.state = state;
}

function persistCurrent(updatedAt = new Date().toISOString(), markChanged = true) {
  localState = saveLocalProgress(localStorage, {
    version: 1,
    updatedAt,
    completed: [...completed],
  }, validTaskIds);
  hasLocalState = true;
  if (markChanged) localRevision += 1;
}

function replaceProgress(state) {
  completed.clear();
  state.completed.forEach((id) => completed.add(id));
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
  if (activeFilter === "today" && task.plannedDay !== currentPlanDay()) return false;
  if (activeFilter.startsWith("day:") && task.plannedDay !== Number(activeFilter.slice(4))) return false;
  if (!searchQuery) return true;
  const haystack = `${task.title} ${task.chapterTitle} ${task.weekTitle}`.toLowerCase();
  return haystack.includes(searchQuery);
}

function taskMarkup(task) {
  const date = dateForDay(task.plannedDay);
  const fileUrl = encodeURI(`file://${task.absolutePath}`);
  return `
    <article class="task${completed.has(task.id) ? " is-complete" : ""}" data-task-id="${task.id}">
      <time class="task-date" datetime="${isoDate(date)}">Day ${String(task.plannedDay).padStart(2, "0")}<br>${displayDate(date)}</time>
      <input class="check" type="checkbox" aria-label="完成：${escapeHtml(task.title)}" ${completed.has(task.id) ? "checked" : ""} />
      <div>
        <h4 class="task-title"><a class="video-link" href="${fileUrl}" title="打开本地视频">${escapeHtml(task.title)}</a></h4>
        <p class="task-path">${escapeHtml(task.relativePath)}</p>
      </div>
      <div class="video-meta">${formatDuration(task.durationSeconds)}<br>视频 ${tasks.indexOf(task) + 1} / ${tasks.length}</div>
    </article>
  `;
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
  document.querySelector("#todayAdvice").textContent = firstOpen ? `继续 Day ${String(firstOpen.plannedDay).padStart(2, "0")}` : "全部完成，准备面试";
  renderHeatmap();
}

function renderHeatmap() {
  const heatmap = document.querySelector("#heatmap");
  const currentDay = currentPlanDay();
  const cells = Array.from({ length: course.targetDays }, (_, index) => {
    const day = index + 1;
    const dayTasks = tasks.filter((task) => task.plannedDay === day);
    const doneCount = dayTasks.filter((task) => completed.has(task.id)).length;
    const ratio = dayTasks.length ? doneCount / dayTasks.length : 0;
    const level = doneCount === 0 ? 0 : ratio === 1 ? 4 : ratio >= .66 ? 3 : ratio >= .33 ? 2 : 1;
    const date = dateForDay(day);
    const selected = activeFilter === `day:${day}`;
    return `<button class="heat-cell${day === currentDay ? " is-today" : ""}${selected ? " is-selected" : ""}" data-level="${level}" data-day="${day}" type="button" role="listitem" title="Day ${String(day).padStart(2, "0")} · ${displayDate(date)} · ${doneCount} / ${dayTasks.length} 个视频" aria-label="Day ${String(day).padStart(2, "0")}，${displayDate(date)}，完成 ${doneCount} / ${dayTasks.length} 个视频"><span>${day}</span></button>`;
  });
  heatmap.innerHTML = cells.join("");
  heatmap.querySelectorAll(".heat-cell").forEach((cell) => {
    cell.addEventListener("click", () => {
      activeFilter = `day:${cell.dataset.day}`;
      document.querySelectorAll(".filter").forEach((item) => item.setAttribute("aria-pressed", "false"));
      render();
    });
  });
  const planDaysWithProgress = Array.from({ length: course.targetDays }, (_, index) => index + 1)
    .filter((day) => tasks.some((task) => task.plannedDay === day && completed.has(task.id))).length;
  const todayTasks = tasks.filter((task) => task.plannedDay === currentDay);
  const todayDone = todayTasks.filter((task) => completed.has(task.id)).length;
  document.querySelector("#heatmapSummary").textContent = `${planDaysWithProgress} / ${course.targetDays} 天有完成记录 · 今日 ${todayDone} / ${todayTasks.length}`;
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
      </button>
      <div class="week-items"></div>
    `;

    const weekItems = section.querySelector(".week-items");
    for (const chapter of week.chapters) {
      const visibleChapterTasks = chapter.tasks.filter(matches);
      if (!visibleChapterTasks.length) continue;
      const doneInChapter = chapter.tasks.filter((task) => completed.has(task.id)).length;
      const chapterElement = document.createElement("section");
      chapterElement.className = "chapter";
      chapterElement.innerHTML = `
        <button class="chapter-heading" type="button" aria-expanded="true">
          <span class="chapter-index">CH ${String(chapter.number).padStart(2, "0")}</span>
          <h3 class="chapter-title">${escapeHtml(chapter.title)}</h3>
          <span class="chapter-count">${doneInChapter} / ${chapter.tasks.length}</span>
        </button>
        <div class="chapter-items">${visibleChapterTasks.map(taskMarkup).join("")}</div>
      `;

      const chapterButton = chapterElement.querySelector(".chapter-heading");
      const chapterItems = chapterElement.querySelector(".chapter-items");
      chapterButton.addEventListener("click", () => {
        const expanded = chapterButton.getAttribute("aria-expanded") === "true";
        chapterButton.setAttribute("aria-expanded", String(!expanded));
        chapterItems.hidden = expanded;
      });
      weekItems.append(chapterElement);
    }

    const weekButton = section.querySelector(".week-heading");
    weekButton.addEventListener("click", () => {
      const expanded = weekButton.getAttribute("aria-expanded") === "true";
      weekButton.setAttribute("aria-expanded", String(!expanded));
      weekItems.hidden = expanded;
    });
    planRoot.append(section);
  }

  if (!visibleTaskCount) planRoot.innerHTML = '<div class="empty">这个筛选条件下没有视频。</div>';
  planRoot.querySelectorAll(".check").forEach((checkbox) => {
    checkbox.addEventListener("change", (event) => {
      const taskId = event.target.closest(".task").dataset.taskId;
      event.target.checked ? completed.add(taskId) : completed.delete(taskId);
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
  const saved = normalizeProgress(await syncClient.put(snapshot), validTaskIds);
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
      localState = saveLocalProgress(localStorage, { ...saved, completed: snapshot }, validTaskIds);
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
