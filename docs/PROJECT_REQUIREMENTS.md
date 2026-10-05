# Project requirements and step-by-step walkthrough

**Project:** AI Developer Agent — Incident Desk  
**Author:** Sajad Ali  
**Document scope:** The implemented local portfolio application, October 2026.

Read this document first if you are new to the project. It explains what we wanted to build, what each part does, and how to follow one request through the code. For installation commands, use the [main README](../README.md).

## 1. What problem does this project explore?

When an application has an incident, a developer usually checks several sources: its health, recent logs and recent deployments. Reading these together helps the developer decide what to investigate next.

This project demonstrates that process with fictional services. It collects evidence, produces a structured incident report, and optionally asks a local language model to suggest a possible cause and relevant checks.

**The goal is to support investigation.** The application does not prove a root cause or repair a service. All health, logs and deployment records come from sample data stored in Java.

### Example

The payment service has these sample facts:

- A deployment changed database connection pool configuration at 10:00.
- Logs report connection acquisition errors and an exhausted pool at 10:05.
- A payment request failed at 10:06.

A reasonable hypothesis is that the configuration change may be related. The evidence does not prove it: slow queries or connections not being released could also be relevant checks. A successful deployment status means deployment succeeded, not that the service stayed healthy afterwards.

## 2. Who is it for?

- A developer learning Spring AI and tool calling through an existing Java application.
- A reviewer who wants to see a working full-stack portfolio project.
- Someone exploring how to keep known facts separate from model-generated suggestions.

It is designed to run locally on one person's computer. There are no user accounts or production monitoring connections.

## 3. Requirements for the current version

These requirements describe implemented behavior. Acceptance checks explain how a reader can recognise that behavior.

| ID | Requirement | Acceptance check |
|---|---|---|
| FR-01 | Display the fictional service catalog. | Payment, order and inventory scenarios appear on the home page. |
| FR-02 | Investigate a selected or typed service. | Submitting a valid name returns a report containing that service name. |
| FR-03 | Collect health, logs and latest deployment for an investigation. | Report evidence and execution trace show the collected information, including missing data where applicable. |
| FR-04 | Offer a demo mode without a model dependency. | Payment investigation succeeds with Ollama stopped and shows `DEMO_TEMPLATE`. |
| FR-05 | Offer local AI interpretation for unhealthy services. | With Ollama running, Local AI returns `OLLAMA` suggestions, or an explicit error if generation fails. |
| FR-06 | Preserve authoritative service identity, health and evidence in Java. | Generated suggestions do not replace the collected report fields. |
| FR-07 | Handle healthy and unknown services directly. | Order returns HEALTHY; an unknown valid name returns UNKNOWN. Both show `RULE_BASED`, even in AI mode. |
| FR-08 | Provide a separate tool-calling lab. | A chat request can lead the model to request a named Java tool; Spring AI executes the tool. |
| FR-09 | Keep recent reports in the current browser. | Reports can be reopened from desktop sidebar history after a refresh; at most eight are retained when browser storage is available. |
| FR-10 | Export the selected report. | Export JSON downloads the report as a `.json` file. |
| FR-11 | Show useful request errors. | Invalid API input returns 400; a model failure displays an error rather than a fabricated report. |
| FR-12 | Offer an optional MCP adapter. | A compatible MCP client can discover and call the three read-only evidence tools. |

### Reliability and usability requirements

| ID | Requirement | Implemented approach |
|---|---|---|
| NFR-01 | Clearly identify fictional information. | Demo notices, `demoData: true`, warnings and analysis-source labels. |
| NFR-02 | Limit caller waiting for model work. | Default 45-second model deadline; model transport read timeout is 50 seconds. |
| NFR-03 | Avoid piling up model requests. | One model job at a time; another request gets 429 while the slot is occupied. |
| NFR-04 | Limit repeated chat tool use. | Three tool executions total and at most one per tool in a request. |
| NFR-05 | Keep model output bounded and validate reports. | 600 output tokens per generation; generated analysis is checked for missing or oversized fields. |
| NFR-06 | Support desktop and mobile use. | Responsive interface and desktop/mobile browser tests. Recent history controls are available in the desktop sidebar. |
| NFR-07 | Make the application reproducible. | Maven wrapper, npm lockfiles, packaging script, automated tests and GitHub Actions. |

A caller timeout does not guarantee that Ollama immediately stops processing. The model slot remains occupied until the background Java task exits. Shape validation checks the structure of generated text; it does not establish its truth.

## 4. Understand the three analysis sources

| Source shown in JSON | When it is used | Does it call the model? |
|---|---|---|
| `DEMO_TEMPLATE` | An unhealthy service in Demo mode | No. Java supplies a predefined explanation and checks. |
| `RULE_BASED` | A healthy or unknown service in either mode | No. Java already has enough information for this response. |
| `OLLAMA` | An unhealthy service in Local AI mode, or a successful tool-lab answer | Yes. The local model generates text. |

**Local AI still uses fictional evidence.** Switching modes changes how suggestions are produced; it does not connect the app to real services.

## 5. What each technology does

| Technology | Its job here |
|---|---|
| Java 21 | Runs backend logic, data types, validation and concurrency controls. |
| Spring Boot | Starts the Java application and exposes HTTP endpoints. |
| Spring AI | Connects Java to the model, converts structured responses and manages chat tool execution. |
| Ollama | Runs the downloaded language model locally. The default model name is `llama3.2`. |
| React | Displays forms, reports, loading states, errors and history in the browser. |
| Vite | Runs the frontend development server and builds frontend files. |
| MCP SDK and Node.js | Run the optional adapter that exposes evidence tools to external MCP clients. |
| JUnit, Mockito and Playwright | Check Java behavior and browser interactions. |
| GitHub Actions | Builds and tests the repository after code changes. |

There is no application database. The fictional catalog lives in Java code; recent report history lives in browser local storage. JDBC and Kafka appear in the sample incident evidence, but this project does not connect to a real database or Kafka broker.

## 6. Follow one investigation, step by step

Example: select `payment-service`, choose **Local AI**, and click **Investigate**.

1. **React reads the form.** It creates this request body:

   ```json
   {"serviceName":"payment-service","mode":"ai"}
   ```

2. **The browser sends an HTTP POST** to `/api/ai/investigate`. During development, Vite forwards `/api` requests to Spring Boot on port 8080.
3. **`ChatController` receives the request.** Spring converts the JSON into an `InvestigationRequest` Java record. The controller calls `InvestigationService`.
4. **Java validates the input.** The name is trimmed and lowercased. Invalid names or unsupported modes produce a 400 response. A valid but unknown name is handled differently: it produces an UNKNOWN report.
5. **Java collects the evidence.** `InvestigationService` calls `getServiceHealth`, `getRecentLogs` and `getLastDeployment`. `ServiceTools` reads the fictional `ServiceCatalog`.
6. **Java decides the analysis path.** Payment is unhealthy and the requested mode is AI, so `SpringAiGateway.analyse` is used. Demo, healthy and unknown paths do not need this model call.
7. **`ModelGate` controls admission and waiting.** If another model job is running, the request receives 429. Otherwise, it runs the work with a deadline.
8. **Spring AI sends the evidence to Ollama.** This investigation analysis makes one model call with no tool definitions. It asks for `suspectedCause` and `recommendedChecks` and converts the result into an `Analysis` record.
9. **Java checks the generated result and assembles the report.** Health and evidence come from Java. AI suggestions are labelled unverified, and the cause is prefixed as an unconfirmed hypothesis.
10. **React displays the report.** It shows the source, evidence, recommendations and trace, and attempts to save a copy in local browser history. If the request fails, it shows the error instead.

The execution trace describes application steps. It is not the model's private reasoning.

## 7. How tool calling differs from an investigation

The investigation form has a fixed Java workflow: it always gathers the three evidence sources. It does not ask the model to decide which evidence methods to call.

The **Tool-calling lab** demonstrates a different workflow:

1. You ask, for example, “Check payment-service health.”
2. Spring AI sends the question and available tool descriptions to the model.
3. The model may request `getServiceHealth` with `payment-service` as its argument.
4. Spring AI executes the registered Java method and returns its result to the model.
5. The model writes a final answer using that result.

The model requests a tool call; Java executes it. The model does not get unrestricted access to your computer. The `@Tool` annotation makes a method available to this tool-calling mechanism. It does not automatically call a method whenever a request arrives.

The lab requires Ollama and has no conversation memory. A follow-up message does not automatically contain your previous conversation.

## 8. Where MCP fits

MCP is a protocol through which a compatible client can discover and use tools.

The optional `mcp/server.mjs` process exposes three tools to such a client. When called, it reads the Spring backend's service snapshot endpoint and returns the relevant evidence. The adapter communicates with its client using standard input/output, and with Spring Boot using HTTP.

The React investigation form does not use MCP. Spring AI's Java tool registration and the separate MCP adapter demonstrate two ways to expose application capabilities. Neither needs to be understood before trying Demo mode.

For configuration, see the [MCP README](../mcp/README.md).

## 9. Understand a report

| Field | Meaning |
|---|---|
| `service`, `health` | Which service was checked and its catalog status. |
| `evidence` | Observed sample health, logs and deployment information. |
| `suspectedCause` | A possible explanation, or a healthy/unknown explanation. It is not a proven diagnosis. |
| `recommendedChecks` | Suggested next investigation steps. None are executed automatically. |
| `demoData` | Always true for this version's fictional evidence. |
| `mode`, `analysisSource` | The requested mode and how the response was actually produced. |
| `id`, `generatedAt` | Report identifier and generation time. |
| `durationMs` | Backend processing time, excluding browser/network overhead. |
| `warnings`, `trace` | Limitations and recorded workflow steps. |

See the [API reference](API.md) for HTTP routes and error codes.

## 10. Manual acceptance walkthrough

Start the application using the [README](../README.md), then perform these checks in order:

1. **Payment / Demo:** expect UNHEALTHY, pool errors, demo-template suggestions and deployment evidence.
2. **Order / Demo:** expect HEALTHY and no failure observed.
3. **Inventory / Demo:** expect UNHEALTHY, consumer lag and timeout evidence.
4. **Unknown name:** enter `unknown-service`; expect UNKNOWN and an explanation that catalog data is unavailable.
5. **Report controls:** expand the trace, export JSON, refresh the page and reopen a report from desktop history.
6. **Payment / Local AI:** start Ollama first; expect `OLLAMA` suggestions or an explicit model error. Do not expect identical wording on every run.
7. **Order / Local AI:** expect `RULE_BASED`; healthy reports deliberately bypass the model.
8. **Tool-calling lab:** ask a health question and inspect the server's `TOOL CALLED` log messages.
9. **Failure behavior:** with Ollama stopped, attempt an unhealthy Local AI investigation. Expect a visible error; Demo mode should still work.

Input checks and model-busy/deadline behavior are also covered by automated tests. The browser may block invalid form submissions before they reach the API, so API validation tests call the server directly.

For verified test results and remaining limits, read [Evaluation](EVALUATION.md). Passing software tests does not prove the quality of a live model's diagnoses.

## 11. Read the code in this order

All Java paths below are under `src/main/java/com/sajad/aideveloperagent/`.

| Order | File | Question to answer while reading |
|---|---|---|
| 1 | `service/ServiceCatalog.java` | Where does the evidence come from? |
| 2 | `tools/ServiceTools.java` | What do the three evidence methods return? |
| 3 | `model/IncidentReport.java` | What does a complete report contain? |
| 4 | `controller/ChatController.java` | Which Java method receives each HTTP request? |
| 5 | `service/InvestigationService.java` | How are evidence and analysis combined? |
| 6 | `service/SpringAiGateway.java` | How do model analysis and tool chat differ? |
| 7 | `service/ModelGate.java` | What happens when the model is busy or slow? |
| 8 | `exception/GlobalExceptionHandler.java` | How do Java failures become HTTP errors? |

Then read `frontend/src/main.jsx` for the browser flow and `mcp/server.mjs` for the optional adapter. Use the [learning guide](LEARNING_GUIDE.md) for small exercises.

## 12. Small glossary

- **API/endpoint:** an address a program calls to request information or an action.
- **JSON:** the text format used to send structured data between this browser and backend.
- **Record:** a Java type used here to hold named data fields.
- **LLM:** the language model that generates text from its input.
- **Prompt:** the instructions and information sent to the model.
- **Structured output:** model text converted into a defined shape such as the `Analysis` record.
- **Tool:** a specific callable capability, such as reading service health.
- **Fixture:** predefined sample data used for demonstrations or tests.
- **Semaphore:** the concurrency control used here to admit only one model job at a time.
- **Timeout:** a limit on how long a caller waits for work.
- **CI:** automated build and test checks run after repository changes.

## 13. Boundaries and possible future work

This version does not include real monitoring, authentication, a persistent server database, RAG, vector search, conversation memory or automatic remediation. It is a portfolio learning project developed with AI assistance.

Possible future requirements include connecting a read-only monitoring API, adding authenticated users, saving reports in a database, and evaluating model quality against a labelled incident dataset. These are ideas, not implemented features or promises.

A useful first extension is much smaller: add another fictional service, update its tests, and explain its complete request flow in your own words.
