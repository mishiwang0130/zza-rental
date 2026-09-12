<script setup lang="ts">
import { computed, ref } from 'vue'
import type { ChatMessage } from '../types/chat'

const props = defineProps<{ message: ChatMessage }>()

const showSources = ref(false)

const isUser = computed(() => props.message.role === 'user')
const hasSources = computed(() => props.message.sources.length > 0)

function formatScore(score?: number | null): string {
  return typeof score === 'number' ? score.toFixed(3) : '-'
}
</script>

<template>
  <div class="message" :class="isUser ? 'message--user' : 'message--assistant'">
    <div class="message__avatar">{{ isUser ? '访客' : 'AI' }}</div>
    <div class="message__body">
      <div class="message__bubble">
        <span v-if="message.content" class="message__text">{{ message.content }}</span>
        <span v-else-if="message.status === 'streaming'" class="message__pending">正在思考…</span>
        <span v-else class="message__pending">（未返回内容）</span>
        <span v-if="message.status === 'streaming' && message.content" class="message__cursor">▍</span>
      </div>

      <p v-if="message.status === 'error'" class="message__error">回答中断，请重试或换一种问法。</p>

      <div v-if="hasSources" class="sources">
        <button class="sources__toggle" type="button" @click="showSources = !showSources">
          参考资料（{{ message.sources.length }}）{{ showSources ? '收起' : '展开' }}
        </button>
        <ul v-if="showSources" class="sources__list">
          <li v-for="(source, index) in message.sources" :key="`${source.documentId}-${index}`">
            <div class="sources__title">
              {{ source.fileName || '未命名文档' }}
              <span class="sources__score">相似度 {{ formatScore(source.score) }}</span>
            </div>
            <p class="sources__snippet">{{ source.snippet }}</p>
          </li>
        </ul>
      </div>
    </div>
  </div>
</template>
