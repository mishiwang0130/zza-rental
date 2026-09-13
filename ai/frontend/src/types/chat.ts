/** 与后端 common 模块的 Result 结构一致：code=200 表示成功 */
export interface ApiResult<T> {
  code: number
  message: string
  data: T
}

export interface SourceRef {
  documentId?: string | null
  fileName?: string | null
  category?: string | null
  score?: number | null
  snippet?: string | null
}

export type MessageRole = 'user' | 'assistant'

export type MessageStatus = 'streaming' | 'done' | 'error'

export interface ChatMessage {
  id: string
  role: MessageRole
  content: string
  sources: SourceRef[]
  status: MessageStatus
  createdAt: number
}

export interface ChatRequestPayload {
  visitorId: string
  conversationId?: string | null
  message: string
  /** 访客选择的城市标签；为空表示不按城市过滤 */
  city?: string | null
}

export interface ChatAnswer {
  conversationId: string
  answer: string
  sources: SourceRef[]
}

/** SSE 事件载荷，与后端 ChatStreamPayload 一一对应 */
export interface StreamMeta {
  conversationId: string
  visitorId: string
}

export interface StreamDelta {
  content: string
}

export interface StreamSources {
  sources: SourceRef[]
}

export interface StreamDone {
  conversationId: string
  elapsedMillis: number
}

export interface StreamError {
  code: string
  message: string
}
