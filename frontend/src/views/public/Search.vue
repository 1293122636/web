<template>
  <n-layout class="search-page">
    <n-layout-header bordered class="header">
      <div class="header-inner">
        <h2 @click="$router.push('/books')" style="cursor:pointer">📚 山东科技大学图书馆</h2>
        <n-space>
          <n-button text @click="$router.push('/login')">登录</n-button>
        </n-space>
      </div>
    </n-layout-header>
    <n-layout-content>
      <div class="content">
        <aside class="sidebar">
          <FacetPanel :facets="store.facets" :active="activeFilters" @select="onFacetSelect" />
        </aside>
        <main class="main">
          <SearchBar @search="onSearch" />
          <n-divider style="margin: 12px 0" />
          <n-space justify="space-between" style="margin-bottom: 12px">
            <span class="result-count">{{ store.total }} 条结果</span>
            <n-pagination v-model:page="store.page" :page-count="totalPages" :page-size="20" @update:page="onPage" />
          </n-space>
          <n-spin :show="store.loading">
            <BookGrid :books="store.results" :loading="store.loading" @select="onBookSelect" />
          </n-spin>
          <n-divider />
          <n-pagination v-model:page="store.page" :page-count="totalPages" :page-size="20" @update:page="onPage" />
        </main>
      </div>
    </n-layout-content>
  </n-layout>
</template>

<script setup lang="ts">
import { onMounted, computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { NLayout, NLayoutHeader, NLayoutContent, NButton, NSpace, NSpin, NDivider, NPagination } from 'naive-ui'
import SearchBar from '../../components/SearchBar.vue'
import FacetPanel from '../../components/FacetPanel.vue'
import BookGrid from '../../components/BookGrid.vue'
import { useBookStore } from '../../stores/books'
import type { BookListParams } from '../../types/api'

const router = useRouter()
const store = useBookStore()
const activeFilters = ref<Record<string, string>>({})

const totalPages = computed(() => Math.max(1, Math.ceil(store.total / 20)))

onMounted(async () => {
  await store.search({})
  await store.updateFacets()
})

async function onSearch(query: string) {
  await store.search({ search: query || undefined })
  await store.updateFacets(query || undefined)
}

async function onFacetSelect(key: string, value: string) {
  if (activeFilters.value[key] === value) {
    delete activeFilters.value[key]
  } else {
    activeFilters.value[key] = value
  }
  const params: BookListParams = { search: store.searchQuery || undefined }
  if (activeFilters.value.campus) params.campus = activeFilters.value.campus
  await store.search(params)
}

function onBookSelect(id: number) { router.push(`/books/${id}`) }
function onPage(p: number) { store.goTo(p) }
</script>

<style scoped>
.search-page { min-height: 100vh; background: var(--n-color-body); }
.header { padding: 0 24px; }
.header-inner { display: flex; align-items: center; justify-content: space-between; height: 56px; }
.header-inner h2 { font-size: 18px; margin: 0; color: var(--n-text-color); }
.content { display: flex; padding: 16px 24px; gap: 24px; }
.sidebar { flex-shrink: 0; }
.main { flex: 1; }
.result-count { font-size: 13px; color: var(--n-text-color-3); }
</style>
