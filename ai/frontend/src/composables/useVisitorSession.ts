import { ref } from 'vue'

const VISITOR_ID_KEY = 'ai-customer.visitorId'
const CONVERSATION_ID_KEY = 'ai-customer.conversationId'

function createUuid(): string {
  if (typeof crypto !== 'undefined' && 'randomUUID' in crypto) {
    return crypto.randomUUID()
  }
  return `visitor-${Date.now()}-${Math.random().toString(16).slice(2, 10)}`
}

/**
 * 匿名访客身份：visitorId 长期保留，conversationId 每次“新对话”重新生成。
 */
export function useVisitorSession() {
  const visitorId = ref(resolveVisitorId())
  const conversationId = ref(localStorage.getItem(CONVERSATION_ID_KEY) ?? '')

  function resolveVisitorId(): string {
    const stored = localStorage.getItem(VISITOR_ID_KEY)
    if (stored) {
      return stored
    }
    const created = createUuid()
    localStorage.setItem(VISITOR_ID_KEY, created)
    return created
  }

  function setConversationId(id: string): void {
    conversationId.value = id
    localStorage.setItem(CONVERSATION_ID_KEY, id)
  }

  function startNewConversation(): void {
    localStorage.removeItem(CONVERSATION_ID_KEY)
    conversationId.value = ''
  }

  return { visitorId, conversationId, setConversationId, startNewConversation }
}
