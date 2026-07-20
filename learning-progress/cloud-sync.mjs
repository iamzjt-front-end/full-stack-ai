export const LOCAL_STATE_KEY = "full-stack-ai-video-progress-v3";
export const LEGACY_STATE_KEY = "full-stack-ai-video-progress-v2";
export const ENDPOINT_KEY = "full-stack-ai-progress-sync-endpoint-v1";
export const SECRET_KEY = "full-stack-ai-progress-sync-secret-v1";

export function normalizeEndpoint(value) {
  const raw = String(value || "").trim();
  if (!raw) throw new TypeError("请填写 Worker 地址");

  let url;
  try {
    url = new URL(raw);
  } catch {
    throw new TypeError("Worker 地址格式不正确");
  }

  const isLocal = ["localhost", "127.0.0.1"].includes(url.hostname);
  if (url.protocol !== "https:" && !(isLocal && url.protocol === "http:")) {
    throw new TypeError("Worker 地址必须使用 HTTPS");
  }

  url.search = "";
  url.hash = "";
  url.pathname = url.pathname.replace(/\/api\/progress\/?$/, "").replace(/\/$/, "");
  return url.toString().replace(/\/$/, "");
}

export function normalizeProgress(value, validIds) {
  const validIdSet = validIds instanceof Set ? validIds : new Set(validIds);
  const completed = Array.isArray(value?.completed)
    ? [...new Set(value.completed.filter((id) => validIdSet.has(id)))]
    : [];
  const updatedAt = typeof value?.updatedAt === "string" && Number.isFinite(Date.parse(value.updatedAt))
    ? new Date(value.updatedAt).toISOString()
    : null;
  return { version: 1, updatedAt, completed };
}

export function loadLocalProgress(storage, validIds, now = () => new Date().toISOString()) {
  try {
    const current = JSON.parse(storage.getItem(LOCAL_STATE_KEY) || "null");
    if (current && Array.isArray(current.completed)) {
      return { exists: true, state: normalizeProgress(current, validIds) };
    }
  } catch {
    // Ignore an unreadable local record and try the legacy format.
  }

  try {
    const legacy = JSON.parse(storage.getItem(LEGACY_STATE_KEY) || "null");
    if (Array.isArray(legacy)) {
      return {
        exists: true,
        state: normalizeProgress({ updatedAt: now(), completed: legacy }, validIds),
      };
    }
  } catch {
    // An invalid legacy value is equivalent to no local state.
  }

  return {
    exists: false,
    state: { version: 1, updatedAt: null, completed: [] },
  };
}

export function saveLocalProgress(storage, state, validIds) {
  const normalized = normalizeProgress(state, validIds);
  storage.setItem(LOCAL_STATE_KEY, JSON.stringify(normalized));
  storage.removeItem(LEGACY_STATE_KEY);
  return normalized;
}

export function chooseInitialProgress(local, remote) {
  if (!remote.updatedAt) return local.exists ? "upload-local" : "already-synced";
  if (!local.exists || !local.state.updatedAt) return "use-remote";
  return Date.parse(local.state.updatedAt) > Date.parse(remote.updatedAt)
    ? "upload-local"
    : "use-remote";
}

export function createProgressClient({ endpoint, secret, fetchImpl = fetch }) {
  const baseUrl = normalizeEndpoint(endpoint);

  async function request(method, body) {
    const response = await fetchImpl(`${baseUrl}/api/progress`, {
      method,
      headers: {
        Authorization: `Bearer ${secret}`,
        ...(body ? { "Content-Type": "application/json" } : {}),
      },
      ...(body ? { body: JSON.stringify(body) } : {}),
    });

    let payload = null;
    try {
      payload = await response.json();
    } catch {
      // A Cloudflare platform error can return non-JSON content.
    }

    if (!response.ok) {
      const message = payload?.error || `云同步请求失败（${response.status}）`;
      const error = new Error(message);
      error.status = response.status;
      throw error;
    }
    return payload;
  }

  return {
    get: () => request("GET"),
    put: (completed) => request("PUT", { completed }),
  };
}
