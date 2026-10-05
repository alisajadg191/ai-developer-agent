# Architecture and design decisions

## Components

| Component | Responsibility |
|---|---|
| `ChatController` | HTTP routes, request dispatch and API metadata |
| `ServiceCatalog` | Validates service identifiers and owns immutable fictional fixtures |
| `ServiceTools` | Read-only access to health, logs and deployments; annotated for Spring AI |
| `InvestigationService` | Collects evidence once, chooses rules/template/model and assembles the authoritative report |
| `SpringAiGateway` | Separate clients for one-pass synthesis and dynamic tool calling |
| `ModelGate` | Single in-flight model task, fail-fast busy response and caller deadline |
| `HttpClientConfig` | Connection and read timeouts for Spring AI HTTP transports |
| `GlobalExceptionHandler` | RFC 9457 problem responses without leaking stack traces |
| React | User interaction, local history and JSON export |
| MCP adapter | Exposes evidence over stdio to compatible local clients |

## Why not ask an agent to do everything?

During development a small local model omitted tools, confused unknown cause with unknown health, and requests sometimes stalled. The observed logs did not establish one root cause for every stall. Restarting Ollama restored simple generation, but long investigations remained unreliable.

This version addresses the application-level failure modes: fixed evidence collection, no model call for healthy/unknown data, a single model call for incident synthesis, a deadline, no model request queue and an independent tool budget for chat. It does not claim to repair every possible Ollama/GPU fault.

The investigation is a **hybrid deterministic/LLM workflow**. The chat lab is the **dynamic tool-calling agent**. Both are useful designs; choose based on whether tool selection actually needs to be dynamic.

## Evidence ownership

Java always supplies report service, health, evidence, demoData and trace. The LLM only proposes `suspectedCause` and `recommendedChecks`. Generated causes are prefixed with “Unconfirmed hypothesis”. This prevents accidental replacement of known fields but does not prove a suggestion is correct. Shape/length checks reject missing or excessive model output. Raw evidence remains visible for review.

There are no write tools or remediation endpoints. System prompts are behaviour guidance, not a security boundary. Input validation, a closed tool set and absence of destructive capabilities enforce the actual limits.

## Timeouts and concurrency

The request waits at most 45 seconds by default. A virtual-thread worker performs the model call. A semaphore admits only one model job. If the caller times out, the slot stays held until the worker finishes; new AI requests fail with 429. HTTP clients have a three-second connect timeout and 50-second read timeout. Ollama can continue computing after a client disconnect, so backend timeouts are not promises of immediate model cancellation.

Chat is capped at three total calls and one per tool, with THROW limit behaviour. The integration test uses a deliberately repeating model stub to verify it stops after the second request for the same tool.

## Persistence and deployment

Reports are not persisted on the server. React saves eight reports to localStorage for convenience. The packaged JAR serves the UI and API from one origin; during development Vite proxies the API. Loopback binding is deliberate: public hosting would require authentication, rate limits, deployment secrets management and a real model-serving plan.

RAG, vector storage, real monitoring adapters and conversation memory are possible later extensions, not implemented features.
