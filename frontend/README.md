# Incident Desk frontend

React + Vite interface for the Spring Boot backend in the parent directory.

```bash
npm ci
npm run dev
```

Open http://127.0.0.1:5173. Start the backend separately on port 8080. Vite proxies `/api` to `http://127.0.0.1:8080`. The page shows backend connection status, which is not a check that Ollama can generate.

Features: three scenario cards, custom service names, demo/AI mode, evidence and execution trace, local report history (up to eight reports), JSON export, tool-calling lab, loading/error states and responsive layout. All report content is rendered as text, never raw HTML.

`npm run build` creates `dist`. The root `scripts/package.sh` bundles this output into the Spring Boot JAR. `npm run preview` previews the production frontend and still requires the backend.

`npm run test:e2e` runs desktop and mobile Chromium checks against the real demo API. First build the backend JAR and install Playwright Chromium as described in the root README. API error display uses an intercepted 504 response, clearly distinct from a live model timeout test.

Report history uses this browser's localStorage. Use Clear in the desktop sidebar to remove it; it is not sent back to the model and is not conversation memory.
