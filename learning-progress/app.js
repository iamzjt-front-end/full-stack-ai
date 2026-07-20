(() => {
  const course = window.COURSE_PROGRESS_DATA;
  const planRoot = document.querySelector("#plan");
  if (!course?.videos?.length) {
    planRoot.innerHTML = '<div class="empty">课程任务数据加载失败，请先运行生成脚本。</div>';
    return;
  }

  const tasks = course.videos;
  const storageKey = "full-stack-ai-video-progress-v2";
  const startDate = new Date(`${course.startDate}T00:00:00+08:00`);
  const completed = new Set(JSON.parse(localStorage.getItem(storageKey) || "[]").filter((id) => tasks.some((task) => task.id === id)));
  let activeFilter = "all";
  let searchQuery = "";

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

  function matches(task) {
    const done = completed.has(task.id);
    if (activeFilter === "open" && done) return false;
    if (activeFilter === "done" && !done) return false;
    if (activeFilter === "today" && task.plannedDay !== currentPlanDay()) return false;
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
        localStorage.setItem(storageKey, JSON.stringify([...completed]));
        render();
      });
    });
    updateOverview();
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
    if (!completed.size || !window.confirm("确定清空全部 479 个视频的学习进度吗？此操作无法恢复。")) return;
    completed.clear();
    localStorage.removeItem(storageKey);
    render();
  });

  const deadline = dateForDay(course.targetDays);
  document.querySelector("#deadlineDate").textContent = `${String(deadline.getMonth() + 1).padStart(2, "0")} 月 ${String(deadline.getDate()).padStart(2, "0")} 日`;
  document.querySelector("#planMeta").textContent = `${tasks.length} 个视频 · ${formatDuration(course.totalDurationSeconds)} · 来自真实课程目录`;
  render();
})();
