# API reference

Base URL: `http://127.0.0.1:8080`. Responses contain fictional data only.

## Investigate

`POST /api/ai/investigate`

```json
{"serviceName":"payment-service","mode":"demo"}
```

`mode` may be `demo` or `ai` and defaults to `demo`. Service names are trimmed, lowercased, and must match `[a-z0-9][a-z0-9-]{0,63}`. A syntactically valid unknown service gets a 200 report with UNKNOWN health; an invalid identifier gets 400.

The response contains `id`, `generatedAt`, `service`, `health`, `evidence`, `suspectedCause`, `recommendedChecks`, `demoData`, `mode`, `analysisSource`, `durationMs`, `warnings`, and `trace`. `durationMs` measures server processing, not network round-trip time. Trace entries record Java evidence steps and the analysis source; they are not model chain-of-thought.

`analysisSource` is `DEMO_TEMPLATE`, `RULE_BASED` or `OLLAMA`. The chosen UI mode is also retained, since AI mode still bypasses the model for healthy and unknown services.

## Chat

`POST /api/ai/chat` with `{"message":"Check payment-service health"}`. The message must contain 1–2000 characters. Returns `answer`, `demoData: true`, and `analysisSource: OLLAMA`. Requires a running model. No conversation history is retained.

## Errors

| HTTP | Meaning |
|---|---|
| 400 | Missing/invalid input or malformed request JSON |
| 429 | Another model job is still running (`MODEL_BUSY`) |
| 502 | Model unavailable, unusable output or tool budget exceeded |
| 503 | Request worker interrupted |
| 504 | Model deadline exceeded (`MODEL_TIMEOUT`) |
| 500 | Unexpected application failure |

Errors use `application/problem+json`, with `status`, `title`, `detail`, `instance` and an application `code` where applicable. A failed model call never silently becomes a successful demo report.

GET convenience endpoints retain tutorial compatibility. Use POST for application integrations; query strings can be retained in browser histories and access logs.
