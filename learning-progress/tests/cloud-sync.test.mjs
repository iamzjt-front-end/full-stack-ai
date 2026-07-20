import assert from "node:assert/strict";
import test from "node:test";

import {
  ENDPOINT_KEY,
  LEGACY_STATE_KEY,
  LOCAL_STATE_KEY,
  chooseInitialProgress,
  createProgressClient,
  loadLocalProgress,
  normalizeEndpoint,
  saveLocalProgress,
} from "../cloud-sync.mjs";

function memoryStorage(initial = {}) {
  const values = new Map(Object.entries(initial));
  return {
    getItem: (key) => values.get(key) ?? null,
    setItem: (key, value) => values.set(key, value),
    removeItem: (key) => values.delete(key),
    values,
  };
}

const ids = new Set(["3a6bbdf77e22", "e919fafd823c"]);

test("normalizes a Worker root URL", () => {
  assert.equal(
    normalizeEndpoint("https://progress.example.workers.dev/api/progress?ignored=yes"),
    "https://progress.example.workers.dev",
  );
  assert.throws(() => normalizeEndpoint("http://progress.example.com"), /HTTPS/);
});

test("migrates the legacy checkbox array without losing progress", () => {
  const storage = memoryStorage({
    [LEGACY_STATE_KEY]: JSON.stringify(["3a6bbdf77e22", "unknown"]),
  });
  const local = loadLocalProgress(storage, ids, () => "2026-07-20T00:00:00.000Z");
  assert.equal(local.exists, true);
  assert.deepEqual(local.state.completed, ["3a6bbdf77e22"]);

  saveLocalProgress(storage, local.state, ids);
  assert.equal(storage.getItem(LEGACY_STATE_KEY), null);
  assert.deepEqual(JSON.parse(storage.getItem(LOCAL_STATE_KEY)).completed, ["3a6bbdf77e22"]);
});

test("does not invent an empty local record on a new browser", () => {
  const storage = memoryStorage();
  const local = loadLocalProgress(storage, ids);
  assert.equal(local.exists, false);
  assert.equal(storage.getItem(LOCAL_STATE_KEY), null);
});

test("selects the newer record during initial synchronization", () => {
  const local = {
    exists: true,
    state: { updatedAt: "2026-07-20T12:00:00.000Z", completed: [] },
  };
  assert.equal(chooseInitialProgress(local, {
    updatedAt: "2026-07-20T11:00:00.000Z",
    completed: ["3a6bbdf77e22"],
  }), "upload-local");
  assert.equal(chooseInitialProgress(local, {
    updatedAt: "2026-07-20T13:00:00.000Z",
    completed: ["3a6bbdf77e22"],
  }), "use-remote");
});

test("sends the dedicated sync code only to the Worker API", async () => {
  let request;
  const client = createProgressClient({
    endpoint: "https://progress.example.workers.dev",
    secret: "sync-only-secret",
    fetchImpl: async (url, options) => {
      request = { url, options };
      return new Response(JSON.stringify({
        version: 1,
        updatedAt: "2026-07-20T12:00:00.000Z",
        completed: [],
      }), { status: 200, headers: { "Content-Type": "application/json" } });
    },
  });
  await client.get();
  assert.equal(request.url, "https://progress.example.workers.dev/api/progress");
  assert.equal(request.options.headers.Authorization, "Bearer sync-only-secret");
  assert.equal(Object.hasOwn(request.options.headers, "X-GitHub-Token"), false);
});
