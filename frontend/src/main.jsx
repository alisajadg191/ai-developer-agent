import React, { useState, useEffect } from "react";
import { createRoot } from "react-dom/client";
import "./style.css";

const HISTORY_KEY = "incident-desk-history-v1";
const sourceLabel = {
  DEMO_TEMPLATE: "Demo template",
  RULE_BASED: "Rule-based result",
  OLLAMA: "Ollama analysis",
};
const badge = (health) =>
  health === "HEALTHY" ? "good" : health === "UNHEALTHY" ? "bad" : "neutral";
function loadHistory() {
  try {
    const v = JSON.parse(localStorage.getItem(HISTORY_KEY) || "[]");
    return Array.isArray(v)
      ? v
          .filter(
            (x) =>
              x &&
              typeof x.id === "string" &&
              Array.isArray(x.evidence) &&
              Array.isArray(x.trace),
          )
          .slice(0, 8)
      : [];
  } catch {
    return [];
  }
}
async function api(path, options = {}) {
  const response = await fetch(path, {
    ...options,
    signal: AbortSignal.timeout(60000),
    headers: { "Content-Type": "application/json", ...options.headers },
  });
  const data = await response.json();
  if (!response.ok)
    throw new Error(
      data.detail || "The request failed. Check the backend logs.",
    );
  return data;
}
function App() {
  const [services, setServices] = useState([]),
    [service, setService] = useState("payment-service"),
    [mode, setMode] = useState("demo");
  const [report, setReport] = useState(null),
    [history, setHistory] = useState(loadHistory),
    [busy, setBusy] = useState(false),
    [error, setError] = useState("");
  const [tab, setTab] = useState("investigate"),
    [status, setStatus] = useState(null),
    [message, setMessage] = useState("Check payment-service health"),
    [answer, setAnswer] = useState("");
  useEffect(() => {
    Promise.all([api("/api/services"), api("/api/status")])
      .then(([s, h]) => {
        setServices(s);
        setStatus(h);
      })
      .catch(() =>
        setError(
          "Backend is unavailable. Start Spring Boot on port 8080, then reload this page.",
        ),
      );
  }, []);
  function remember(value) {
    const next = [value, ...history.filter((x) => x.id !== value.id)].slice(
      0,
      8,
    );
    setHistory(next);
    try {
      localStorage.setItem(HISTORY_KEY, JSON.stringify(next));
    } catch {
      /* Storage may be disabled. */
    }
  }
  async function investigate(event) {
    event.preventDefault();
    if (busy) return;
    setBusy(true);
    setError("");
    setReport(null);
    try {
      const result = await api("/api/ai/investigate", {
        method: "POST",
        body: JSON.stringify({ serviceName: service, mode }),
      });
      setReport(result);
      remember(result);
    } catch (e) {
      setError(
        e.name === "TimeoutError"
          ? "The request timed out. The server may still be finishing; wait before retrying."
          : e.message,
      );
    } finally {
      setBusy(false);
    }
  }
  async function chat(event) {
    event.preventDefault();
    if (busy) return;
    setBusy(true);
    setError("");
    setAnswer("");
    try {
      const result = await api("/api/ai/chat", {
        method: "POST",
        body: JSON.stringify({ message }),
      });
      setAnswer(result.answer);
    } catch (e) {
      setError(
        e.name === "TimeoutError"
          ? "The request timed out. Check Ollama before retrying."
          : e.message,
      );
    } finally {
      setBusy(false);
    }
  }
  function download() {
    const url = URL.createObjectURL(
      new Blob([JSON.stringify(report, null, 2)], { type: "application/json" }),
    );
    const a = document.createElement("a");
    a.href = url;
    a.download = `${report.service}-report.json`;
    a.click();
    setTimeout(() => URL.revokeObjectURL(url), 1000);
  }
  return (
    <div className="shell">
      <aside className="sidebar">
        <a className="brand" href="/" aria-label="Incident Desk home">
          <span className="brand-icon">⌁</span>
          <span>
            Incident Desk<small>AI DEVELOPER AGENT</small>
          </span>
        </a>
        <div className="nav-label">WORKSPACE</div>
        <nav aria-label="Main navigation">
          <button
            disabled={busy}
            className={tab === "investigate" ? "nav active" : "nav"}
            onClick={() => {
              setTab("investigate");
              setError("");
            }}
          >
            <span>▤</span> Investigations
          </button>
          <button
            disabled={busy}
            className={tab === "chat" ? "nav active" : "nav"}
            onClick={() => {
              setTab("chat");
              setError("");
            }}
          >
            <span>⌘</span> Tool-calling lab
          </button>
        </nav>
        <div className="nav-label history-heading">
          RECENT REPORTS{" "}
          <button
            disabled={busy || !history.length}
            title="Clear local history"
            onClick={() => {
              setHistory([]);
              try {
                localStorage.removeItem(HISTORY_KEY);
              } catch {}
            }}
          >
            Clear
          </button>
        </div>
        <div className="history">
          {history.length ? (
            history.map((h) => (
              <button
                disabled={busy}
                key={h.id}
                onClick={() => {
                  setReport(h);
                  setTab("investigate");
                  setError("");
                }}
              >
                <span className={"dot " + badge(h.health)}></span>
                <span>
                  {h.service}
                  <small>
                    {sourceLabel[h.analysisSource]} ·{" "}
                    {new Date(h.generatedAt).toLocaleTimeString([], {
                      hour: "2-digit",
                      minute: "2-digit",
                    })}
                  </small>
                </span>
              </button>
            ))
          ) : (
            <p>Your investigations will appear here.</p>
          )}
        </div>
        <div className="sidebar-bottom">
          <span className="local-indicator"></span> Built to run locally
          <p>
            Read-only tools. Fictional services.
            <br />
            Your history stays in this browser.
          </p>
          <a
            href="https://github.com/alisajadg191/ai-developer-agent"
            target="_blank"
            rel="noreferrer"
          >
            View project on GitHub ↗
          </a>
        </div>
      </aside>
      <main>
        <header className="topbar">
          <span>
            Workspace <span className="slash">/</span>{" "}
            {tab === "investigate" ? "Investigations" : "Tool-calling lab"}
          </span>
          <span className="connection">
            <i className={status ? "connected" : ""}></i>
            {status ? "Backend connected" : "Backend not connected"}
          </span>
        </header>
        <div className="content">
          <div className="heading">
            <div>
              <div className="eyebrow">DEVELOPER OPERATIONS</div>
              <h1>
                {tab === "investigate"
                  ? "Understand the incident."
                  : "See tools in action."}
              </h1>
              <p>
                {tab === "investigate"
                  ? "Collect the evidence. Separate facts from hypotheses. Decide what to check next."
                  : "Ask a local model to choose from three read-only Java tools."}
              </p>
            </div>
            <span className="version">PORTFOLIO DEMO / V1</span>
          </div>
          <div className="notice">
            <span>ⓘ</span>
            <div>
              <strong>A safe place to investigate.</strong> Every service, log
              and deployment is simulated. No production systems are connected.
            </div>
          </div>
          {error && (
            <div role="alert" className="error">
              <strong>Request could not complete</strong>
              <p>{error}</p>
            </div>
          )}
          {tab === "investigate" ? (
            <>
              <section className="catalog" aria-label="Demo services">
                {services.map((s) => (
                  <button
                    disabled={busy}
                    key={s.name}
                    className={
                      "service-card " + (service === s.name ? "selected" : "")
                    }
                    onClick={() => setService(s.name)}
                    aria-pressed={service === s.name}
                  >
                    <span className="service-top">
                      <span className={"dot " + badge(s.health)}></span>
                      <span>
                        {s.health === "HEALTHY"
                          ? "Healthy baseline"
                          : "Incident scenario"}
                      </span>
                      <span className="arrow">↗</span>
                    </span>
                    <strong>{s.name}</strong>
                    <small>{s.description}</small>
                  </button>
                ))}
              </section>
              <form className="panel launch" onSubmit={investigate}>
                <div className="panel-title">
                  <span className="step">01</span>
                  <h2>Start an investigation</h2>
                </div>
                <div className="launch-fields">
                  <label className="service-input">
                    Service name
                    <input
                      required
                      pattern="[A-Za-z0-9][A-Za-z0-9-]{0,63}"
                      maxLength={64}
                      value={service}
                      onChange={(e) => setService(e.target.value)}
                      disabled={busy}
                      placeholder="payment-service"
                    />
                  </label>
                  <fieldset disabled={busy}>
                    <legend>Analysis mode</legend>
                    <div className="segmented">
                      <button
                        type="button"
                        aria-pressed={mode === "demo"}
                        className={mode === "demo" ? "chosen" : ""}
                        onClick={() => setMode("demo")}
                      >
                        Demo
                      </button>
                      <button
                        type="button"
                        aria-pressed={mode === "ai"}
                        className={mode === "ai" ? "chosen" : ""}
                        onClick={() => setMode("ai")}
                      >
                        Local AI
                      </button>
                    </div>
                  </fieldset>
                  <button
                    className="primary"
                    disabled={busy || !service.trim()}
                    type="submit"
                  >
                    {busy ? (
                      <>
                        <span className="spinner" /> Investigating…
                      </>
                    ) : (
                      <>
                        Investigate <span>→</span>
                      </>
                    )}
                  </button>
                </div>
                <p className="hint">
                  {mode === "demo"
                    ? "Instant, rule-based demo. No model or API key required."
                    : `Uses ${status?.model || "your local model"} for unhealthy services. Healthy and unknown services use rules. A 45-second deadline applies by default.`}
                </p>
              </form>
              <div aria-live="polite">
                {busy ? (
                  <div className="panel working">
                    <span className="spinner" />
                    <h2>Building your report</h2>
                    <p>
                      Reading service evidence
                      {mode === "ai"
                        ? " and requesting local model analysis"
                        : ""}
                      . Please keep this to one request at a time.
                    </p>
                  </div>
                ) : report ? (
                  <section className="panel report">
                    <div className="report-header">
                      <div>
                        <div className="eyebrow">INVESTIGATION REPORT</div>
                        <h2>{report.service}</h2>
                        <div className="report-meta">
                          <span className={"pill " + badge(report.health)}>
                            {report.health}
                          </span>
                          <span>{sourceLabel[report.analysisSource]}</span>
                          <span>{report.durationMs} ms</span>
                        </div>
                      </div>
                      <button className="secondary" onClick={download}>
                        Export JSON ↓
                      </button>
                    </div>
                    <div className="report-body">
                      <div className="analysis">
                        <h3>Suspected cause</h3>
                        <p className="cause">{report.suspectedCause}</p>
                        <h3>Recommended checks</h3>
                        {report.recommendedChecks.length ? (
                          <ol className="checks">
                            {report.recommendedChecks.map((c, i) => (
                              <li key={i}>{c}</li>
                            ))}
                          </ol>
                        ) : (
                          <p className="muted">
                            No corrective checks suggested for this healthy
                            baseline.
                          </p>
                        )}
                        <div className="warning-list">
                          {report.warnings.map((w, i) => (
                            <p key={i}>ⓘ {w}</p>
                          ))}
                        </div>
                      </div>
                      <div className="evidence">
                        <h3>
                          Observed evidence{" "}
                          <span>{report.evidence.length}</span>
                        </h3>
                        {report.evidence.map((e, i) => (
                          <div className="evidence-line" key={i}>
                            <span>{String(i + 1).padStart(2, "0")}</span>
                            <code>{e}</code>
                          </div>
                        ))}
                      </div>
                    </div>
                    <details className="trace">
                      <summary>
                        Execution trace{" "}
                        <span>
                          Java-collected evidence · {report.trace.length} steps
                        </span>
                      </summary>
                      <ol>
                        {report.trace.map((t, i) => (
                          <li key={i}>
                            <strong>{t.tool}</strong>
                            <span>{t.detail}</span>
                            <small>{t.status}</small>
                          </li>
                        ))}
                      </ol>
                    </details>
                    <div className="report-footer">
                      {new Date(report.generatedAt).toLocaleString()}
                      <span>Report {report.id.slice(0, 8)} · DEMO DATA</span>
                    </div>
                  </section>
                ) : (
                  <section className="panel empty">
                    <div className="empty-icon">▤</div>
                    <h2>Evidence first. Answers second.</h2>
                    <p>
                      Select a service and run an investigation. Your report
                      will show the source evidence, suspected cause and next
                      checks.
                    </p>
                    <div className="empty-flow">
                      <span>Service health</span>
                      <span>Application logs</span>
                      <span>Deployment history</span>
                    </div>
                  </section>
                )}
              </div>
            </>
          ) : (
            <section className="panel lab">
              <div className="panel-title">
                <span className="step">⌘</span>
                <h2>Tool-calling lab</h2>
              </div>
              <p>
                This endpoint lets the model select tools. It needs Ollama,
                allows at most three tool calls, and has no conversation memory.
                Answers can be wrong; the investigation form preserves evidence
                more reliably.
              </p>
              <form onSubmit={chat}>
                <label htmlFor="question">Your request</label>
                <textarea
                  id="question"
                  required
                  maxLength={2000}
                  value={message}
                  onChange={(e) => setMessage(e.target.value)}
                  disabled={busy}
                />
                <button className="primary" disabled={busy}>
                  {busy ? "Waiting for local model…" : "Ask the agent →"}
                </button>
              </form>
              {answer && (
                <div className="chat-answer" aria-live="polite">
                  <div className="eyebrow">MODEL RESPONSE · DEMO DATA</div>
                  <p>{answer}</p>
                </div>
              )}
            </section>
          )}
          <footer className="page-footer">
            <span>Java 21 / Spring AI / React / Ollama</span>
            <span>Built by Sajad Ali</span>
          </footer>
        </div>
      </main>
    </div>
  );
}
createRoot(document.getElementById("root")).render(<App />);
