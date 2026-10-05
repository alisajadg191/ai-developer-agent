import { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js";
import { StdioServerTransport } from "@modelcontextprotocol/sdk/server/stdio.js";
import { z } from "zod";

const base = new URL(process.env.AGENT_BASE_URL || "http://127.0.0.1:8080");
if (
  !["http:", "https:"].includes(base.protocol) ||
  !["127.0.0.1", "localhost", "[::1]"].includes(base.hostname)
) {
  throw new Error(
    "AGENT_BASE_URL must use a loopback host. This demo does not connect to remote production systems.",
  );
}
const server = new McpServer({ name: "incident-desk", version: "1.0.0" });
const schema = {
  serviceName: z
    .string()
    .regex(/^[a-z0-9][a-z0-9-]{0,63}$/)
    .describe("Demo service name, e.g. payment-service"),
};
const annotations = {
  readOnlyHint: true,
  destructiveHint: false,
  idempotentHint: true,
  openWorldHint: false,
};
function tool(name, description, select) {
  server.registerTool(
    name,
    { description, inputSchema: schema, annotations },
    async ({ serviceName }) => {
      try {
        const response = await fetch(
          new URL(
            `/api/services/${encodeURIComponent(serviceName)}/snapshot`,
            base,
          ),
          {
            signal: AbortSignal.timeout(5000),
            redirect: "error",
          },
        );
        if (!response.ok)
          throw new Error(`Backend returned HTTP ${response.status}`);
        const snapshot = await response.json();
        return {
          content: [
            {
              type: "text",
              text: JSON.stringify({ demoData: true, ...select(snapshot) }),
            },
          ],
        };
      } catch (error) {
        return {
          isError: true,
          content: [
            {
              type: "text",
              text: `Could not read demo evidence: ${error.message}`,
            },
          ],
        };
      }
    },
  );
}
tool(
  "getServiceHealth",
  "Read fictional service health. UNKNOWN means no fixture exists.",
  (s) => ({ service: s.health.service, health: s.health.health }),
);
tool(
  "getRecentLogs",
  "Read fictional application logs; an empty list means unavailable.",
  (s) => ({ service: s.health.service, logs: s.logs }),
);
tool(
  "getLastDeployment",
  "Read a fictional deployment; null means unavailable. Timing is not proof of causation.",
  (s) => ({ service: s.health.service, deployment: s.deployment }),
);
// stdout is reserved exclusively for MCP protocol messages.
await server.connect(new StdioServerTransport());
