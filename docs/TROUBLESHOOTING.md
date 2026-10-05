# Troubleshooting

## Java version mismatch

`java -version` and `./mvnw -version` must both show Java 21. On macOS, `export JAVA_HOME=$(/usr/libexec/java_home -v 21)` selects an installed JDK 21 for that shell. Also set IntelliJ's Project SDK and Maven runner JDK. Do not change the project to Java 26 to work around a local mismatch.

## Cannot connect to port 8080

Start `AiDeveloperAgentApplication` or `./mvnw spring-boot:run`. Wait for the Tomcat startup message. Ollama uses port 11434; it does not start Spring Boot. Use `curl http://127.0.0.1:8080/api/status` to check the backend.

## Homepage returns 404

During development the UI is at port 5173. To serve it at 8080, run `bash scripts/package.sh` and start the generated JAR.

## Missing or blank serviceName

Use the UI or send the complete quoted URL:

```bash
curl 'http://127.0.0.1:8080/api/ai/investigate?serviceName=order-service'
```

An omitted parameter and an empty parameter both return 400, with different explanations.

## Ollama responds to /api/ps but generation hangs

A loaded-model listing is not a generation readiness check. Stop submitting investigations. Test a short direct generation using the command in the root README. If it hangs, quit and reopen Ollama, then test again before retrying AI mode. On macOS inspect `tail -n 60 ~/.ollama/logs/server.log`.

The application uses 127.0.0.1 to avoid hostname resolution for Ollama. A Netty native DNS warning alone does not establish the cause of a generation stall.

## 504 or 429

504 means the caller deadline was reached. The model worker may still be finishing. 429 prevents a second queued job. Wait, check Ollama logs, or use demo mode. If the runner remains stalled, restart Ollama. Restarting only the browser does not cancel server work. Reducing model size may help on limited hardware; increasing the timeout is not a diagnosis.

## AI report contains a bad hypothesis

Inspect the raw evidence and mark the result as a failed evaluation. Known health/evidence remain Java-owned, but suggestions are not guaranteed correct. Do not treat a configuration change before an incident as proof that it caused the incident.

## Updating an existing local checkout

Run `git status` first. Preserve any uncommitted work before pulling. If you have no local changes, use `git pull --ff-only`. If you have changes, commit them on a backup branch or stash them with `git stash push -u -m "local tutorial work"`, then pull. Do not use a hard reset to resolve differences unless you deliberately intend to discard them.

## MCP server appears to wait silently

Stdio MCP servers wait for a client to send protocol messages. Configure the client with the absolute Node and server paths. The Java backend must also be running. Run `npm test --prefix mcp` to verify protocol integration independently.
