<template>
  <n-input ref="inputRef" v-model:value="value" :placeholder="placeholder" clearable
    @keyup.enter="onEnter" @clear="onClear" size="large" :autofocus="true" />
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { NInput } from 'naive-ui'

const props = defineProps<{ placeholder?: string }>()
const emit = defineEmits<{ scan: [barcode: string]; clear: [] }>()

const value = ref('')
const inputRef = ref()

function onEnter() {
  if (value.value.trim()) {
    emit('scan', value.value.trim())
    value.value = ''
  }
}
function onClear() { emit('clear'); inputRef.value?.focus() }
</script>
