import assert from "node:assert/strict";
import test from "node:test";

import worker, { isAllowedOrigin, normalizeCompleted } from "../src/index.mjs";

function createEnvironment(overrides = {}) {
  const records = new Map();
  return {
    ALLOWED_ORIGINS: "https://iamzjt-front-end.github.io",
    SYNC_SECRET: "correct-horse-battery-staple",
    PROGRESS: {
      async get(key, type) {
        const value = records.get(key);
        return type === "json" && value ? JSON.parse(value) : value;
      },
      async put(key, value) {
        records.set(key, value);
      },
    },
    ...overrides,
  };
}

function progressRequest(method = "GET", options = {}) {
  const headers = new Headers({
    Origin: "https://iamzjt-front-end.github.io",
    Authorization: "Bearer correct-horse-battery-staple",
    ...options.headers,
  });
  return new Request("https://sync.example/api/progress", {
    method,
    headers,
    body: options.body,
  });
}

test("allows the GitHub Pages origin and local development only", () => {
  const env = createEnvironment();
  assert.equal(isAllowedOrigin("https://iamzjt-front-end.github.io", env), true);
  assert.equal(isAllowedOrigin("http://127.0.0.1:4173", env), true);
  assert.equal(isAllowedOrigin("https://attacker.example", env), false);
});

test("normalizes valid task ids and rejects malformed data", () => {
  assert.deepEqual(normalizeCompleted(["e919fafd823c", "3a6bbdf77e22", "e919fafd823c"]), [
    "3a6bbdf77e22",
    "e919fafd823c",
  ]);
  assert.throws(() => normalizeCompleted(["not-an-id"]), /invalid task id/);
});

test("requires the configured sync secret", async () => {
  const response = await worker.fetch(progressRequest("GET", {
    headers: { Authorization: "Bearer wrong-secret" },
  }), createEnvironment());
  assert.equal(response.status, 401);
});

test("writes progress to KV and reads it back", async () => {
  const env = createEnvironment();
  const putResponse = await worker.fetch(progressRequest("PUT", {
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      completed: ["e919fafd823c", "3a6bbdf77e22"],
      completedAt: {
        "e919fafd823c": "2026-07-20",
        "3a6bbdf77e22": "2026-07-21",
      },
    }),
  }), env);
  assert.equal(putResponse.status, 200);
  const saved = await putResponse.json();
  assert.deepEqual(saved.completed, ["3a6bbdf77e22", "e919fafd823c"]);
  assert.deepEqual(saved.completedAt, {
    "e919fafd823c": "2026-07-20",
    "3a6bbdf77e22": "2026-07-21",
  });
  assert.match(saved.updatedAt, /^\d{4}-\d{2}-\d{2}T/);

  const getResponse = await worker.fetch(progressRequest(), env);
  assert.equal(getResponse.status, 200);
  assert.deepEqual(await getResponse.json(), saved);
});

test("rejects browser requests from an unrelated site", async () => {
  const response = await worker.fetch(progressRequest("GET", {
    headers: { Origin: "https://attacker.example" },
  }), createEnvironment());
  assert.equal(response.status, 403);
  assert.equal(response.headers.get("Access-Control-Allow-Origin"), null);
});
