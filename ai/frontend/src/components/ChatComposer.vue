<script setup lang="ts">
import { ref } from 'vue'

const props = defineProps<{ sending: boolean; maxLength?: number }>()
const emit = defineEmits<{ submit: [text: string] }>()

const draft = ref('')
const limit = props.maxLength ?? 2000

function submit(): void {
  const text = draft.value.trim()
  if (!text || props.sending) {
    return
  }
  emit('submit', text)
  draft.value = ''
}

function onKeydown(event: KeyboardEvent): void {
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault()
    submit()
  }
}
</script>

<template>
  <div class="composer">
    <textarea
      v-model="draft"
      class="composer__input"
      rows="2"
      :maxlength="limit"
      placeholder="例如：有哪些两室一厅？退租要提前多久申请？（Enter 发送，Shift+Enter 换行）"
      @keydown="onKeydown"
    />
    <button class="composer__send" type="button" :disabled="sending || !draft.trim()" @click="submit">
      {{ sending ? '回答中…' : '发送' }}
    </button>
  </div>
</template>
