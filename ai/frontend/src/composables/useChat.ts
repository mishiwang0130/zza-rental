import { computed, reactive, ref } from 'vue'
import { clearConversation, streamChat } from '../api/chat'
import { useVisitorSession } from './useVisitorSession'
import type { ChatMessage } from '../types/chat'

function createMessageId(): string {
  return `msg-${Date.now()}-${Math.random().toString(16).slice(2, 8)}`
}

/**
 * 聊天状态机：发送消息 → 追加 SSE 增量 → 收尾 / 报错。
 */
export function useChat() {
  const { visitorId, conversationId, setConversationId, startNewConversation } = useVisitorSession()
  const messages = ref<ChatMessage[]>([])
  const sending = ref(false)
  const errorMessage = ref('')
  const elapsedMillis = ref(0)

  const hasMessages = computed(() => messages.value.length > 0)

  async function send(text: string): Promise<void> {
    const content = text.trim()
    if (!content || sending.value) {
      return
    }
    errorMessage.value = ''
    sending.value = true
    messages.value.push({
      id: createMessageId(),
      role: 'user',
      content,
      sources: [],
      status: 'done',
      createdAt: Date.now(),
    })
    // 用 reactive 包一层：直接改 push 进 ref 数组的普通对象不会触发视图更新
    const assistant = reactive<ChatMessage>({
      id: createMessageId(),
      role: 'assistant',
      content: '',
      sources: [],
      status: 'streaming',
      createdAt: Date.now(),
    })
    messages.value.push(assistant)

    try {
      await streamChat(
        {
          visitorId: visitorId.value,
          conversationId: conversationId.value || null,
          message: content,
        },
        {
          onMeta: (meta) => setConversationId(meta.conversationId),
          onDelta: (delta) => {
            assistant.content += delta.content
          },
          onSources: (payload) => {
            assistant.sources = payload.sources ?? []
          },
          onDone: (payload) => {
            assistant.status = 'done'
            elapsedMillis.value = Math.round(payload.elapsedMillis)
          },
          onError: (payload) => {
            assistant.status = 'error'
            assistant.content = assistant.content || payload.message
            errorMessage.value = payload.message
          },
        },
      )
      if (assistant.status === 'streaming') {
        assistant.status = 'done'
        if (!assistant.content) {
          assistant.content = '（服务端没有返回内容，请稍后重试）'
        }
      }
    } catch (error) {
      assistant.status = 'error'
      const message = error instanceof Error ? error.message : '网络异常，请稍后重试'
      assistant.content = assistant.content || message
      errorMessage.value = message
    } finally {
      sending.value = false
    }
  }

  async function newConversation(): Promise<void> {
    const current = conversationId.value
    messages.value = []
    errorMessage.value = ''
    elapsedMillis.value = 0
    startNewConversation()
    if (current) {
      // 清空后端会话记忆，失败不影响前端已经开启新对话
      try {
        await clearConversation(current)
      } catch {
        // 忽略：历史记忆有 TTL，下次新对话也不会再带上旧 conversationId
      }
    }
  }

  return {
    messages,
    sending,
    hasMessages,
    errorMessage,
    elapsedMillis,
    visitorId,
    conversationId,
    send,
    newConversation,
  }
}
