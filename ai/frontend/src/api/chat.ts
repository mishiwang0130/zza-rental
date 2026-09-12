import { API_BASE_URL, ApiError, request } from './http'
import type {
  ChatAnswer,
  ChatRequestPayload,
  StreamDelta,
  StreamDone,
  StreamError,
  StreamMeta,
  StreamSources,
} from '../types/chat'

export interface StreamHandlers {
  onMeta?: (payload: StreamMeta) => void
  onDelta?: (payload: StreamDelta) => void
  onSources?: (payload: StreamSources) => void
  onDone?: (payload: StreamDone) => void
  onError?: (payload: StreamError) => void
}

/**
 * SSE 流式聊天。用 fetch + ReadableStream 而不是 EventSource，因为需要 POST 并且要能携带请求体。
 */
export async function streamChat(
  payload: ChatRequestPayload,
  handlers: StreamHandlers,
  signal?: AbortSignal,
): Promise<void> {
  const response = await fetch(`${API_BASE_URL}/api/chat/stream`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', Accept: 'text/event-stream' },
    body: JSON.stringify(payload),
    signal,
  })
  if (!response.ok || !response.body) {
    throw new ApiError(`连接 AI 服务失败（HTTP ${response.status}）`)
  }

  const reader = response.body.getReader()
  const decoder = new TextDecoder('utf-8')
  let buffer = ''
  while (true) {
    const { value, done } = await reader.read()
    if (done) {
      break
    }
    buffer += decoder.decode(value, { stream: true })
    // SSE 以空行分隔事件
    const blocks = buffer.split(/\r?\n\r?\n/)
    buffer = blocks.pop() ?? ''
    blocks.forEach((block) => dispatchEvent(block, handlers))
  }
  if (buffer.trim()) {
    dispatchEvent(buffer, handlers)
  }
}

export function askOnce(payload: ChatRequestPayload): Promise<ChatAnswer> {
  return request<ChatAnswer>('/api/chat', { method: 'POST', body: JSON.stringify(payload) })
}

export function clearConversation(conversationId: string): Promise<void> {
  return request<void>(`/api/chat/conversations/${encodeURIComponent(conversationId)}`, {
    method: 'DELETE',
  })
}

function dispatchEvent(block: string, handlers: StreamHandlers): void {
  let eventName = 'message'
  const dataLines: string[] = []
  for (const line of block.split(/\r?\n/)) {
    if (line.startsWith('event:')) {
      eventName = line.slice(6).trim()
    } else if (line.startsWith('data:')) {
      dataLines.push(line.slice(5).trim())
    }
  }
  if (dataLines.length === 0) {
    return
  }
  const raw = dataLines.join('\n')
  switch (eventName) {
    case 'meta':
      handlers.onMeta?.(JSON.parse(raw) as StreamMeta)
      break
    case 'delta':
      handlers.onDelta?.(JSON.parse(raw) as StreamDelta)
      break
    case 'sources':
      handlers.onSources?.(JSON.parse(raw) as StreamSources)
      break
    case 'done':
      handlers.onDone?.(JSON.parse(raw) as StreamDone)
      break
    case 'error':
      handlers.onError?.(JSON.parse(raw) as StreamError)
      break
    default:
      break
  }
}
