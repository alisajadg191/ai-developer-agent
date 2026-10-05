# Incident Desk MCP server

An optional **stdio MCP server**, implemented with the official MCP SDK. It exposes three read-only tools backed by the same Java evidence API:

- `getServiceHealth(serviceName)`
- `getRecentLogs(serviceName)`
- `getLastDeployment(serviceName)`

All returned information is fictional demo data. No model or API key is needed by this adapter. The MCP client/host supplies its own model if desired. The Spring Boot investigation endpoint does not use this adapter internally.

## Setup

Start Spring Boot on port 8080, then:

```bash
npm ci --prefix mcp
```

Configure a local MCP client that supports stdio with an entry like this (replace the absolute paths):

```json
{
  "mcpServers": {
    "incident-desk": {
      "command": "/absolute/path/to/node",
      "args": ["/absolute/path/to/ai-developer-agent/mcp/server.mjs"],
      "env": {"AGENT_BASE_URL": "http://127.0.0.1:8080"}
    }
  }
}
```

Use `which node` to find your Node path on macOS/Linux. The exact settings location depends on your MCP client. The client launches the process; running `npm start --prefix mcp` manually waits for protocol messages on stdin and is not an HTTP server.

The adapter allows only loopback backend hosts, rejects redirects, validates service names, and uses a five-second HTTP timeout. An unavailable backend returns an MCP tool error, not fabricated evidence. Tool annotations identify read-only, idempotent operations. stdout contains only MCP protocol traffic.

## Verify

```bash
npm test --prefix mcp
```

The integration test launches a real stdio MCP client and server against a fixture HTTP backend. It verifies discovery, health, unknown-service logs, input rejection and backend outage handling.
