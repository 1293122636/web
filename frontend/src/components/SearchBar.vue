<template>
  <n-space vertical size="small">
    <div class="search-row">
      <n-select v-model:value="searchType" :options="typeOptions" style="width: 120px" size="small" />
      <n-input v-model:value="query" placeholder="搜索书名/作者/ISBN" clearable @keyup.enter="doSearch" @clear="doSearch" />
      <n-button type="primary" @click="doSearch" size="small">搜索</n-button>
    </div>
    <n-space v-if="filters?.campus" size="small">
      <n-tag closable @close="clearFilter('campus')">校区: {{ filters?.campus }}</n-tag>
    </n-space>
  </n-space>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { NSpace, NSelect, NInput, NButton, NTag } from 'naive-ui'

const props = defineProps<{ filters?: Record<string, string> }>()
const emit = defineEmits<{ search: [query: string, type: string]; clearFilter: [key: string] }>()

const query = ref('')
const searchType = ref('all')
const typeOptions = [
  { label: '全部检索', value: 'all' },
  { label: '题名', value: 'title' },
  { label: '作者', value: 'author' },
  { label: 'ISBN', value: 'isbn' },
]

function doSearch() { emit('search', query.value, searchType.value) }
function clearFilter(key: string) { emit('clearFilter', key) }
</script>

<style scoped>
.search-row { display: flex; gap: 8px; align-items: center; }
</style>
