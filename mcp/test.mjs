import { test } from "node:test";
import assert from "node:assert/strict";
import { createServer } from "node:http";
import { Client } from "@modelcontextprotocol/sdk/client/index.js";
import { StdioClientTransport } from "@modelcontextprotocol/sdk/client/stdio.js";

test("real MCP transport: discover tools, call fixture, reject invalid input and report outage", async () => {
  const backend = createServer((req, res) => {
    res.setHeader("Content-Type", "application/json");
    const unknown = req.url.includes("unknown-service");
    res.end(
      JSON.stringify({
        health: {
          service: unknown ? "unknown-service" : "payment-service",
          health: unknown ? "UNKNOWN" : "UNHEALTHY",
        },
        logs: unknown ? [] : ["fixture log"],
        deployment: unknown ? null : { version: "2.4.1" },
      }),
    );
  });
  await new Promise((resolve) => backend.listen(0, "127.0.0.1", resolve));
  const transport = new StdioClientTransport({
    command: process.execPath,
    args: [new URL("./server.mjs", import.meta.url).pathname],
    env: {
      ...process.env,
      AGENT_BASE_URL: `http://127.0.0.1:${backend.address().port}`,
    },
  });
  const client = new Client({ name: "integration-test", version: "1.0.0" });
  try {
    await client.connect(transport);
    const tools = await client.listTools();
    assert.equal(tools.tools.length, 3);
    assert.ok(tools.tools.every((t) => t.annotations.readOnlyHint));
    const health = await client.callTool({
      name: "getServiceHealth",
      arguments: { serviceName: "payment-service" },
    });
    assert.equal(JSON.parse(health.content[0].text).health, "UNHEALTHY");
    const logs = await client.callTool({
      name: "getRecentLogs",
      arguments: { serviceName: "unknown-service" },
    });
    assert.deepEqual(JSON.parse(logs.content[0].text).logs, []);
    const invalid = await client.callTool({
      name: "getServiceHealth",
      arguments: { serviceName: "../../etc/passwd" },
    });
    assert.equal(invalid.isError, true);
    await new Promise((resolve) => backend.close(resolve));
    const unavailable = await client.callTool({
      name: "getLastDeployment",
      arguments: { serviceName: "payment-service" },
    });
    assert.equal(unavailable.isError, true);
  } finally {
    await client.close();
    backend.close();
  }
});
