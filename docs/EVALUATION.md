# Evaluation

Validation performed on 5 October 2026.

| Check | Result |
|---|---|
| Maven clean verify, Java 21 | Passed: 12 tests, no failures |
| React production build | Passed |
| MCP SDK client/server integration tests | Passed |
| Packaged JAR HTTP acceptance | Passed: 7 checks |
| Packaged JAR serves the frontend | Passed |
| Desktop/mobile Playwright suite | Passed: all 4 tests in GitHub Actions |
| Live Ollama inference quality | Not evaluated in this environment |

## What the checks establish

Backend tests cover input validation, authoritative health and evidence, healthy and unknown baselines, model deadlines, busy admission, and error responses. The actual Spring AI client is exercised against an Ollama-protocol HTTP stub for structured output, invalid JSON and repeated tool calls. These are integration checks, not real-model evaluations.

The seven packaged-app acceptance checks cover payment, order, inventory and unknown services, plus three invalid requests. MCP tests use the official SDK to discover and invoke tools, including invalid input and unavailable-backend errors.

The browser suite covers reports, JSON export, healthy/unknown states, mobile overflow and visible AI errors. The local execution environment blocked browser launch; the suite subsequently passed on a GitHub-hosted runner. [Verified workflow run](https://github.com/alisajadg191/ai-developer-agent/actions/runs/37305971153) tested commit `d76aa67` including packaging, backend tests, MCP and all four browser checks.

## Evaluate your local model

Start Ollama and confirm that its direct generation endpoint responds. Run payment and inventory investigations in Local AI mode three times each. Check that hypotheses remain uncertain, recommendations relate to the supplied evidence, and no invented evidence appears. Run a simple health question in the tool-calling lab and inspect the backend tool-call logs.

The model must not describe a deployment as a proven cause. A structurally valid report may still contain an incorrect hypothesis. Java preserves the original evidence and health; it does not fact-check generated suggestions. No real monitoring or production-remediation capability is claimed.
