# AI Developer Agent

A Java learning project for building a developer and production-support assistant with Spring Boot, Spring AI and local Ollama models.

## Current milestone

Initial Spring Initializr scaffold and Ollama configuration. No chat endpoint or agent tools have been implemented yet.

## Stack

- Java 21
- Maven (wrapper included)
- Spring Boot 4.1.1
- Spring AI 2.0.1
- Spring Web MVC
- Ollama with `llama3.2` by default

Generated with https://start.spring.io using Maven, Java 21, Spring Web and Ollama. The generated Boot version suffix was corrected to the published Maven version `4.1.1`.

## Run locally

Ensure `java -version` and `./mvnw -version` both show Java 21. Set IntelliJ's project SDK and Maven runner JDK to 21 too.

1. Install and start Ollama.
2. Download the model:
   ```bash
   ollama pull llama3.2
   ```
3. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```

The application starts on port 8080. A browser request to `/` returns 404 at this stage because there is no controller yet.

Configuration defaults to `http://localhost:11434`. Override with `OLLAMA_BASE_URL` or `OLLAMA_MODEL` environment variables. Do not commit secrets.

## Verify

```bash
./mvnw test
```

The generated context-load test does not send prompts or download models. End-to-end model testing will be added alongside the chat endpoint.

## Next milestones

1. Add a basic chat endpoint and test it against Ollama.
2. Add a service-health Java tool using demo data.
3. Extend to logs, deployment details and multi-step investigation.
4. Add structured results, evaluation and guardrails.
5. Explore MCP and a React interface.

These are planned features, not implemented capabilities.
