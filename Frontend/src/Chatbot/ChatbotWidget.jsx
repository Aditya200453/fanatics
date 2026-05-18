import { useEffect, useMemo, useRef, useState } from "react";
import "./Chatbot.css";
import { BOT_NAME, FLOWS } from "./Chatbotflows";

export default function ChatbotWidget({
  agent = {
    mode: "whatsapp", // "whatsapp" | "tel" | "mailto" | "url"
    value:
      "https://wa.me/919999999999?text=Hi%20I%20need%20help%20with%20my%20clinic%20query",
  },
}) {
  const [open, setOpen] = useState(false);
  const [input, setInput] = useState("");
  const [handoff, setHandoff] = useState(false);

  const [messages, setMessages] = useState(() => []);
  const listRef = useRef(null);

  const normalizedOptions = useMemo(() => {
    const map = new Map();
    Object.entries(FLOWS).forEach(([flowId, flow]) => {
      if (flow?.options?.length) {
        flow.options.forEach((o) => map.set(o.label.toLowerCase(), o.id));
      }
      if (flow?.next?.length) {
        flow.next.forEach((o) => map.set(o.label.toLowerCase(), o.id));
      }
    });
    return map;
  }, []);

  const pushBot = (text, options = null) => {
    setMessages((prev) => [
      ...prev,
      { id: crypto.randomUUID(), from: "bot", text, options },
    ]);
  };

  const pushUser = (text) => {
    setMessages((prev) => [
      ...prev,
      { id: crypto.randomUUID(), from: "user", text },
    ]);
  };

  const scrollToBottom = () => {
    requestAnimationFrame(() => {
      if (!listRef.current) return;
      listRef.current.scrollTop = listRef.current.scrollHeight;
    });
  };
  // ✅ Close chatbot on ESC key press
useEffect(() => {
  const handleEsc = (e) => {
    if (e.key === "Escape") {
      setOpen(false);
    }
  };

  window.addEventListener("keydown", handleEsc);

  return () => {
    window.removeEventListener("keydown", handleEsc);
  };
}, []);
  // ✅ Greeting typing animation ONLY when chat opens first time
  useEffect(() => {
    if (!open) return;
    if (messages.length > 0) return; // already started, don't reset history

    const root = FLOWS.root;

    // 1) show typing bubble
    setMessages([
      {
        id: crypto.randomUUID(),
        from: "bot",
        typing: true,
      },
    ]);

    // 2) after delay show greeting + options
    const t = setTimeout(() => {
      setMessages([
        {
          id: crypto.randomUUID(),
          from: "bot",
          text: root.message,
          options: root.options,
        },
      ]);
    }, 900);

    return () => clearTimeout(t);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [open]);

  useEffect(() => {
    scrollToBottom();
  }, [messages, open]);

  const runFlow = (flowId) => {
  const flow = FLOWS[flowId];
  if (!flow) return;

  // 1️⃣ Show typing indicator
  const typingId = crypto.randomUUID();
  setMessages((prev) => [
    ...prev,
    {
      id: typingId,
      from: "bot",
      typing: true,
    },
  ]);

  // 2️⃣ After delay, replace typing with actual response
  setTimeout(() => {
    setMessages((prev) => {
      // remove typing bubble
      const withoutTyping = prev.filter((m) => m.id !== typingId);

      const nextMessages = [...withoutTyping];

      // bot answer
      if (flow.answer) {
        nextMessages.push({
          id: crypto.randomUUID(),
          from: "bot",
          text: flow.answer,
        });
      }

      // handoff handling
      if (flow.handoff) {
        setHandoff(true);
      }

      // follow‑up options
      if (flow.next?.length) {
        nextMessages.push({
          id: crypto.randomUUID(),
          from: "bot",
          text: "What would you like to do next?",
          options: flow.next,
        });
      } else {
        nextMessages.push({
          id: crypto.randomUUID(),
          from: "bot",
          text: "Anything else I can help with?",
          options: FLOWS.root.options,
        });
      }

      return nextMessages;
    });
  }, 700); // ⏱️ delay (700ms feels natural)
};

  const onOptionClick = (opt) => {
    setInput("");
    pushUser(opt.label);

    if (opt.id !== "agent") setHandoff(false);
    runFlow(opt.id);
  };

  const handleSend = () => {
    const text = input.trim();
    if (!text) return;

    pushUser(text);
    setInput("");

    // If user typed exactly a supported option label
    const matched = normalizedOptions.get(text.toLowerCase());
    if (matched) {
      if (matched !== "agent") setHandoff(false);
      runFlow(matched);
      return;
    }

    // Otherwise: agent handoff
    setHandoff(true);
    pushBot(
      "I can help with the options in the menu. For this question, I’ll connect you to a support agent."
    );
    pushBot("Choose an option below or connect to an agent:", [
      { id: "agent", label: "Talk to an agent" },
      { id: "root", label: "Back to main menu" },
    ]);
  };

  const openAgent = () => {
    if (agent.mode === "tel") window.location.href = `tel:${agent.value}`;
    else if (agent.mode === "mailto")
      window.location.href = `mailto:${agent.value}`;
    else window.open(agent.value, "_blank");
  };

  return (
    <>
      {/* ✅ Floating Bootstrap Chat Button */}
      {!open && (
        <button
          className="btn btn-primary rounded-circle cb-fab-bs"
          onClick={() => setOpen(true)}
          aria-label="Open chatbot"
          type="button"
        >
          <i className="bi bi-robot"></i>
        </button>
      )}


      {/* Panel */}
      {open && (
        <div className="cb-panel" role="dialog" aria-label="Clinic chatbot">
          <div className="cb-header">
            <div className="cb-title">
              <div className="cb-name">{BOT_NAME}</div>
              <div className="cb-sub">Quick help • Fixed answers</div>
            </div>

            {/* ✅ right-side medical icon */}
            <span className="cb-header-icon" title="Clinic Assist">
              <i className="bi bi-heart-pulse-fill" />
            </span>

            <button
              className="cb-close"
              onClick={() => setOpen(false)}
              aria-label="Close"
              type="button"
            >
              ×
            </button>
          </div>

          <div className="cb-messages" ref={listRef}>
            {messages.map((m) => (
              <div
                key={m.id}
                className={`cb-msg ${m.from === "user" ? "user" : "bot"}`}
              >
                {/* ✅ typing bubble */}
                {m.typing ? (
                  <div className="cb-bubble cb-typing" aria-label="Maddy is typing">
                    <span className="dot" />
                    <span className="dot" />
                    <span className="dot" />
                  </div>
                ) : (
                  <div className="cb-bubble">{m.text}</div>
                )}

                {/* ✅ options */}
                {m.options?.length ? (
                  <div className="cb-options">
                    {m.options.map((opt) => (
                      <button
                        key={opt.id + opt.label}
                        className="cb-option"
                        onClick={() => onOptionClick(opt)}
                        type="button"
                      >
                        {opt.label}
                      </button>
                    ))}
                  </div>
                ) : null}
              </div>
            ))}
          </div>

          {/* Agent handoff bar */}
          {handoff && (
            <div className="cb-handoff">
              <div className="cb-handoffText">Need human help?</div>
              <button className="cb-agentBtn" onClick={openAgent} type="button">
                Connect to Agent
              </button>
            </div>
          )}

          {/* Input */}
          <div className="cb-inputRow">
            <input
              className="cb-input"
              value={input}
              onChange={(e) => setInput(e.target.value)}
              placeholder="Type your message…"
              onKeyDown={(e) => {
                if (e.key === "Enter") handleSend();
              }}
            />
            <button className="cb-send" onClick={handleSend} type="button">
              Send
            </button>
          </div>
        </div>
      )}
    </>
  );
}
