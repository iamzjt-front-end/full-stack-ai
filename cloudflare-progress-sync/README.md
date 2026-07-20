# Learning progress sync worker

This Worker stores one small progress record in Cloudflare KV. GitHub Pages stays responsible for the UI.

## Cloudflare dashboard setup

1. Import this GitHub repository as a Worker.
2. Use `node --test cloudflare-progress-sync/tests/*.test.mjs learning-progress/tests/*.test.mjs` as the build command.
3. Use `npx wrangler@latest deploy` as the deploy command.
4. After the first deployment, add an encrypted Worker secret named `SYNC_SECRET`.
5. Open `/health` on the Worker URL, then configure that URL and the same sync code in the learning-progress page.

The KV namespace is declared without an ID on purpose. Wrangler 4.45 or newer provisions it automatically during the first deployment.
