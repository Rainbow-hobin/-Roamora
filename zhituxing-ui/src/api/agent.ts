// 智途星 · AI 旅行规划 Agent 对接层
// 对应 travel-agent 的 FastAPI 服务（api/main.py），经 Vite 代理 /agent -> http://localhost:8000

export interface AgentChatResult {
  session_id: string
  answer: string
  current_agent: string | null
  needs_clarification: boolean
  elapsed_ms: number
}

export interface AgentStreamHandlers {
  onStart?: () => void
  onAgent?: (agent: string, isComplete: boolean) => void
  onDone?: (result: AgentChatResult) => void
  onError?: (message: string) => void
}

const AGENT_BASE = '/agent'

async function parseError(resp: Response): Promise<Error> {
  let detail = ''
  try {
    const data = (await resp.json()) as { detail?: string }
    detail = data.detail || ''
  } catch {
    /* 非 JSON 响应 */
  }
  return new Error(detail || `AI 规划服务异常（HTTP ${resp.status}）`)
}

export async function agentHealth(): Promise<{ status: string }> {
  let resp: Response
  try {
    resp = await fetch(`${AGENT_BASE}/api/health`)
  } catch {
    throw new Error('无法连接 AI 规划服务，请确认其已启动（http://localhost:8000）')
  }
  if (!resp.ok) throw await parseError(resp)
  return resp.json()
}

export async function agentChat(
  message: string,
  sessionId?: string,
): Promise<AgentChatResult> {
  let resp: Response
  try {
    resp = await fetch(`${AGENT_BASE}/api/chat`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ message, session_id: sessionId ?? null }),
    })
  } catch {
    throw new Error('无法连接 AI 规划服务，请确认其已启动（http://localhost:8000）')
  }
  if (!resp.ok) throw await parseError(resp)
  return resp.json()
}

export async function agentChatStream(
  message: string,
  sessionId: string | undefined,
  handlers: AgentStreamHandlers,
  signal?: AbortSignal,
): Promise<void> {
  let resp: Response
  try {
    resp = await fetch(`${AGENT_BASE}/api/chat/stream`, {
      signal,
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ message, session_id: sessionId ?? null }),
    })
  } catch {
    throw new Error('无法连接 AI 规划服务，请确认其已启动（http://localhost:8000）')
  }
  if (!resp.ok || !resp.body) throw await parseError(resp)

  const reader = resp.body.getReader()
  const decoder = new TextDecoder()
  let buffer = ''

  let completed = false
  const handle = (event: string, data: Record<string, unknown>) => {
    if (event === 'start') {
      handlers.onStart?.()
    } else if (event === 'agent') {
      handlers.onAgent?.(String(data.agent ?? ''), Boolean(data.is_complete))
    } else if (event === 'final') {
      completed = true
      handlers.onDone?.({
        session_id: String(data.session_id ?? ''),
        answer: String(data.answer ?? ''),
        current_agent: data.current_agent ? String(data.current_agent) : null,
        needs_clarification: Boolean(data.needs_clarification),
        elapsed_ms: Number(data.elapsed_ms ?? 0),
      })
    } else if (event === 'error') {
      completed = true
      handlers.onError?.(String(data.message ?? ''))
    }
  }

  while (true) {
    const { done, value } = await reader.read()
    if (done) break
    buffer += decoder.decode(value, { stream: true })
    let idx: number
    while ((idx = buffer.indexOf('\n\n')) >= 0) {
      const chunk = buffer.slice(0, idx)
      buffer = buffer.slice(idx + 2)
      let event = 'message'
      let dataText = ''
      for (const line of chunk.split('\n')) {
        if (line.startsWith('event:')) event = line.slice(6).trim()
        else if (line.startsWith('data:')) dataText += line.slice(5).trim()
      }
      if (!dataText) continue
      try {
        handle(event, JSON.parse(dataText))
      } catch {
        /* 单条解析失败不阻断整体 */
      }
    }
  }
  if (!completed) throw new Error('连接中断，方案未完成，请重新发送')
}