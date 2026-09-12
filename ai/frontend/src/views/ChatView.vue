<script setup lang="ts">
import { nextTick, ref, watch } from 'vue'
import ChatComposer from '../components/ChatComposer.vue'
import ChatMessageItem from '../components/ChatMessageItem.vue'
import { useChat } from '../composables/useChat'

const { messages, sending, hasMessages, errorMessage, elapsedMillis, visitorId, conversationId, send, newConversation } =
  useChat()

const listRef = ref<HTMLElement | null>(null)

watch(
  () => messages.value.map((message) => message.content.length).join(','),
  async () => {
    await nextTick()
    const element = listRef.value
    if (element) {
      element.scrollTop = element.scrollHeight
    }
  },
)

function onNewConversation(): void {
  void newConversation()
}
</script>

<template>
  <div class="chat">
    <header class="chat__header">
      <div>
        <h1 class="chat__title">公寓智能客服</h1>
        <p class="chat__subtitle">
          匿名体验，无需登录 · 会话 {{ conversationId || '新会话' }} · 访客 {{ visitorId.slice(0, 8) }}
        </p>
      </div>
      <button class="chat__new" type="button" :disabled="sending" @click="onNewConversation">新对话</button>
    </header>

    <main ref="listRef" class="chat__list">
      <div v-if="!hasMessages" class="chat__empty">
        <p class="chat__empty-title">你好，我是公寓智能客服</p>
        <p>可以问我：有哪些空房、某套房子的详情、入住与退租规则、费用标准、怎么看房预约。</p>
      </div>
      <ChatMessageItem v-for="message in messages" :key="message.id" :message="message" />
    </main>

    <footer class="chat__footer">
      <p v-if="errorMessage" class="chat__error">{{ errorMessage }}</p>
      <ChatComposer :sending="sending" @submit="send" />
      <p v-if="elapsedMillis" class="chat__meta">上一轮回答耗时约 {{ elapsedMillis }} ms</p>
    </footer>
  </div>
</template>
