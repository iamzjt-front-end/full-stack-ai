const PROGRESS_KEY = "learning-progress:v1";
const ID_PATTERN = /^[a-f0-9]{12}$/;
const DATE_PATTERN = /^\d{4}-\d{2}-\d{2}$/;
const MAX_COMPLETED_ITEMS = 1000;

function allowedOrigins(env) {
  return String(env.ALLOWED_ORIGINS || "")
    .split(",")
    .map((origin) => origin.trim())
    .filter(Boolean);
}

export function isAllowedOrigin(origin, env) {
  if (!origin) return false;
  if (allowedOrigins(env).includes(origin)) return true;
  return /^http:\/\/(localhost|127\.0\.0\.1)(:\d+)?$/.test(origin);
}

function corsHeaders(origin) {
  return {
    "Access-Control-Allow-Origin": origin,
    "Access-Control-Allow-Methods": "GET, PUT, OPTIONS",
    "Access-Control-Allow-Headers": "Authorization, Content-Type",
    "Access-Control-Max-Age": "86400",
    "Cache-Control": "no-store",
    "Vary": "Origin",
  };
}

function jsonResponse(payload, status, origin = "") {
  const headers = {
    "Content-Type": "application/json; charset=utf-8",
    ...(origin ? corsHeaders(origin) : { "Cache-Control": "no-store" }),
  };
  return new Response(JSON.stringify(payload), { status, headers });
}

async function secureEqual(value, expected) {
  const encoder = new TextEncoder();
  const [valueHash, expectedHash] = await Promise.all([
    crypto.subtle.digest("SHA-256", encoder.encode(value)),
    crypto.subtle.digest("SHA-256", encoder.encode(expected)),
  ]);
  const left = new Uint8Array(valueHash);
  const right = new Uint8Array(expectedHash);
  let difference = left.length ^ right.length;
  for (let index = 0; index < Math.max(left.length, right.length); index += 1) {
    difference |= (left[index] || 0) ^ (right[index] || 0);
  }
  return difference === 0;
}

async function isAuthorized(request, secret) {
  const authorization = request.headers.get("Authorization") || "";
  const match = authorization.match(/^Bearer\s+(.+)$/i);
  return Boolean(match && await secureEqual(match[1], secret));
}

export function normalizeCompleted(value) {
  if (!Array.isArray(value) || value.length > MAX_COMPLETED_ITEMS) {
    throw new TypeError("completed must be an array with at most 1000 items");
  }

  const completed = [...new Set(value)];
  if (completed.some((id) => typeof id !== "string" || !ID_PATTERN.test(id))) {
    throw new TypeError("completed contains an invalid task id");
  }
  return completed.sort();
}

export function normalizeCompletedAt(value, completed) {
  if (!value || typeof value !== "object" || Array.isArray(value)) return {};
  const completedAt = {};
  for (const id of completed) {
    if (value[id] !== undefined && (typeof value[id] !== "string" || !DATE_PATTERN.test(value[id]))) {
      throw new TypeError("completedAt contains an invalid date");
    }
    if (value[id]) completedAt[id] = value[id];
  }
  return completedAt;
}

async function handleProgress(request, env, origin) {
  if (!env.SYNC_SECRET) {
    return jsonResponse({ error: "SYNC_SECRET is not configured" }, 503, origin);
  }
  if (!await isAuthorized(request, env.SYNC_SECRET)) {
    return jsonResponse({ error: "同步码不正确" }, 401, origin);
  }

  if (request.method === "GET") {
    const saved = await env.PROGRESS.get(PROGRESS_KEY, "json");
    return jsonResponse(saved || {
      version: 1,
      updatedAt: null,
      completed: [],
      completedAt: {},
    }, 200, origin);
  }

  if (request.method === "PUT") {
    let body;
    try {
      body = await request.json();
    } catch {
      return jsonResponse({ error: "请求内容不是有效 JSON" }, 400, origin);
    }

    let completed;
    let completedAt;
    try {
      completed = normalizeCompleted(body?.completed);
      completedAt = normalizeCompletedAt(body?.completedAt, completed);
    } catch (error) {
      return jsonResponse({ error: error.message }, 400, origin);
    }

    const progress = {
      version: 1,
      updatedAt: new Date().toISOString(),
      completed,
      completedAt,
    };
    await env.PROGRESS.put(PROGRESS_KEY, JSON.stringify(progress));
    return jsonResponse(progress, 200, origin);
  }

  return jsonResponse({ error: "Method not allowed" }, 405, origin);
}

export default {
  async fetch(request, env) {
    const url = new URL(request.url);
    const origin = request.headers.get("Origin") || "";

    if (url.pathname === "/health") {
      return jsonResponse({ ok: true, storage: "cloudflare-kv" }, 200);
    }

    if (url.pathname !== "/api/progress") {
      return jsonResponse({ error: "Not found" }, 404);
    }

    if (!isAllowedOrigin(origin, env)) {
      return jsonResponse({ error: "Origin not allowed" }, 403);
    }

    if (request.method === "OPTIONS") {
      return new Response(null, { status: 204, headers: corsHeaders(origin) });
    }

    return handleProgress(request, env, origin);
  },
};
