# Learn the project at your own pace

You do not need to understand every file at once. Start with demo mode and use a debugger.

## Suggested reading order

1. **REST and records:** read `ChatController`, `ServiceHealth`, `IncidentReport`. Send one request. Explain how JSON becomes Java and back.
2. **Evidence:** read `ServiceCatalog` and `ServiceTools`. Call a tool directly in a test. These methods are ordinary Java; `@Tool` describes them to Spring AI.
3. **Workflow:** read `InvestigationService`. Trace payment, order and unknown service. Notice which paths never use an LLM.
4. **Prompts and structured output:** read `SpringAiGateway.analyse`. Learn system/user messages and `.entity(Analysis.class)`. JSON structure does not guarantee factual correctness.
5. **Tool calling:** read `SpringAiGateway.chat`. The model requests a method name and arguments; Spring AI executes Java and sends the result back. The LLM cannot directly execute arbitrary Java.
6. **Reliability:** read `ModelGate` and its tests. Learn a semaphore, virtual-thread task and deadline. A caller timeout and completed background work are different events.
7. **React:** read `frontend/src/main.jsx`. Follow form -> fetch -> loading/error state -> report. Local history is not model memory.
8. **MCP:** read `mcp/server.mjs`. MCP standardises tool discovery and calls. It is not an LLM, RAG system or replacement for your application logic.

## Small exercises

- Add a new fictional service with a healthy baseline. Verify its report without Ollama.
- Change one payment log and confirm the exact evidence appears in the report.
- Send a blank name, an unknown name and a name containing spaces. Explain each response.
- Run a model request with Ollama stopped. Inspect the visible error instead of returning a guessed answer.
- Compare demo mode and AI mode. Explain `DEMO_TEMPLATE`, `RULE_BASED` and `OLLAMA`.
- Connect an MCP client and call `getRecentLogs`. Identify every process involved.

## Interview explanation you should be able to give

“I built a local developer-support demo using Java, Spring Boot, Spring AI and React. Java collects simulated health, logs and deployment evidence. For unhealthy services, a local Ollama model can suggest causes and checks. I preserve known facts outside model-generated output, enforce deadlines and tool budgets, and expose read-only evidence through an MCP server. Tests cover API contracts, failures and the model protocol with a stub. It is a portfolio project with fictional data, not a production monitoring system.”

Learn the code before claiming independent mastery. The implementation was completed with AI assistance; adapting it, debugging it and explaining trade-offs are valuable next steps.

## Primary documentation

- https://docs.spring.io/spring-ai/reference/api/chatclient.html
- https://docs.spring.io/spring-ai/reference/api/tools.html
- https://docs.spring.io/spring-ai/reference/api/chat/ollama-chat.html
- https://docs.ollama.com/
- https://react.dev/learn
- https://modelcontextprotocol.io/docs/develop/build-server

Use tutorials for intuition, then check documentation against the versions in this repository.
