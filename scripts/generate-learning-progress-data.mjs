import { createHash } from "node:crypto";
import { execFileSync } from "node:child_process";
import { readdir, writeFile } from "node:fs/promises";
import path from "node:path";
import { fileURLToPath } from "node:url";

const repoRoot = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..");
const courseRoot = path.resolve(process.argv[2] || "/Users/iamzjt/Desktop/Java+AI全栈工程师体系课");
const outputPath = path.join(repoRoot, "learning-progress-data.js");
const collator = new Intl.Collator("zh-CN", { numeric: true, sensitivity: "base" });

async function listVideos(directory) {
  const entries = await readdir(directory, { withFileTypes: true });
  const sortedEntries = entries.sort((a, b) => collator.compare(a.name, b.name));
  const videos = [];

  for (const entry of sortedEntries) {
    const absolutePath = path.join(directory, entry.name);
    if (entry.isDirectory()) {
      videos.push(...await listVideos(absolutePath));
    } else if (entry.isFile() && path.extname(entry.name).toLowerCase() === ".mp4") {
      videos.push(absolutePath);
    }
  }
  return videos;
}

function durationOf(filePath) {
  const value = execFileSync("/opt/homebrew/bin/ffprobe", [
    "-v", "error",
    "-show_entries", "format=duration",
    "-of", "default=nw=1:nk=1",
    filePath,
  ], { encoding: "utf8" }).trim();
  return Math.round(Number(value));
}

const absoluteVideos = await listVideos(courseRoot);
const videos = absoluteVideos.map((absolutePath) => {
  const relativePath = path.relative(courseRoot, absolutePath);
  const segments = relativePath.split(path.sep);
  const weekDirectory = segments[0];
  const chapterDirectory = segments.length > 2 ? segments[1] : "未分章";
  const weekMatch = weekDirectory.match(/^【第(\d+)周\s*】\s*(.*)$/);
  const chapterMatch = chapterDirectory.match(/^(\d+)-(\d+)\s*(.*)$/);
  const durationSeconds = durationOf(absolutePath);

  return {
    id: createHash("sha1").update(relativePath).digest("hex").slice(0, 12),
    week: weekMatch ? Number(weekMatch[1]) : 0,
    weekTitle: weekMatch ? weekMatch[2].trim() : weekDirectory,
    chapter: chapterMatch ? Number(chapterMatch[2]) : 0,
    chapterTitle: chapterMatch ? chapterMatch[3].trim() : chapterDirectory,
    title: path.basename(absolutePath, path.extname(absolutePath)),
    durationSeconds,
    relativePath,
    absolutePath,
  };
});

const totalDurationSeconds = videos.reduce((sum, video) => sum + video.durationSeconds, 0);
let elapsedSeconds = 0;
for (const video of videos) {
  video.plannedDay = Math.min(45, Math.floor((elapsedSeconds / totalDurationSeconds) * 45) + 1);
  elapsedSeconds += video.durationSeconds;
}

const payload = {
  generatedAt: new Date().toISOString(),
  sourceRoot: courseRoot,
  startDate: "2026-07-20",
  targetDays: 45,
  totalDurationSeconds,
  videos,
};

await writeFile(outputPath, `window.COURSE_PROGRESS_DATA = ${JSON.stringify(payload, null, 2)};\n`, "utf8");
console.log(`Generated ${videos.length} video tasks (${(totalDurationSeconds / 3600).toFixed(2)} hours) at ${outputPath}`);
