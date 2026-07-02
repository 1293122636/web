<template>
  <n-layout class="search-page">
    <n-layout-header bordered class="header">
      <div class="header-inner">
        <div class="brand" @click="$router.push('/books')">
          <!-- Library SVG icon -->
          <svg class="brand-icon" width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/>
            <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/>
            <line x1="8" y1="7" x2="16" y2="7"/>
            <line x1="8" y1="11" x2="14" y2="11"/>
          </svg>
          <div>
            <h2>山东科技大学图书馆</h2>
            <span class="brand-sub">Shandong University of Science and Technology</span>
          </div>
        </div>
        <n-space>
          <n-button text @click="$router.push('/login')">
            <template #icon><n-icon><PersonOutline /></n-icon></template>登录
          </n-button>
        </n-space>
      </div>
    </n-layout-header>
    <n-layout-content>
      <div class="content">
        <aside class="sidebar">
          <n-text depth="3" class="sidebar-title">筛选</n-text>
          <FacetPanel :facets="store.facets" :active="activeFilters" @select="onFacetSelect" />
        </aside>
        <main class="main">
          <div class="search-area">
            <n-input
              v-model:value="searchInput"
              placeholder="搜索书名、作者、ISBN..."
              size="large"
              clearable
              @keyup.enter="onSearch(searchInput)"
              @clear="onSearch('')"
              class="search-input"
            >
              <template #prefix><n-icon :size="20"><SearchOutline /></n-icon></template>
            </n-input>
            <n-button size="large" type="primary" @click="onSearch(searchInput)" class="search-btn">
              搜索
            </n-button>
          </div>
          <div class="result-bar">
            <n-text depth="3" v-if="!store.loading">{{ store.total }} 条结果</n-text>
            <n-text depth="3" v-else>搜索中...</n-text>
            <n-pagination v-model:page="store.page" :page-count="totalPages" :page-size="20" @update:page="onPage" />
          </div>
          <BookGrid :books="store.results" :loading="store.loading" @select="onBookSelect" />
          <n-pagination v-if="store.total > 20" v-model:page="store.page" :page-count="totalPages" :page-size="20" @update:page="onPage" style="justify-content:center;margin-top:16px" />
        </main>
      </div>
    </n-layout-content>
  </n-layout>
</template>

<script setup lang="ts">
import { onMounted, computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { NIcon } from 'naive-ui'
import { SearchOutline, PersonOutline } from '@vicons/ionicons5'
import FacetPanel from '../../components/FacetPanel.vue'
import BookGrid from '../../components/BookGrid.vue'
import { useBookStore } from '../../stores/books'
import type { BookListParams } from '../../types/api'

const router = useRouter()
const store = useBookStore()
const searchInput = ref('')
const activeFilters = ref<Record<string, string>>({})
const totalPages = computed(() => Math.max(1, Math.ceil(store.total / 20)))

onMounted(async () => {
  await store.search({})
  await store.updateFacets()
})

async function onSearch(query: string) {
  searchInput.value = query
  // Merge current activeFilters to preserve campus/category filters
  const params: BookListParams = { ...activeFilters.value }
  if (query) params.search = query
  else delete params.search
  await store.search(params)
  await store.updateFacets({ search: query || undefined })
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
.header {
  padding: 0 28px; height: 64px; display: flex; align-items: center;
  background: linear-gradient(135deg, #5e6ad2 0%, #7c6fdb 100%);
}
.header-inner { display: flex; align-items: center; justify-content: space-between; width: 100%; }
.brand { display: flex; align-items: center; gap: 12px; cursor: pointer; }
.brand-icon { width: 28px; height: 28px; color: rgba(255,255,255,0.85); flex-shrink: 0; }
.brand h2 { margin: 0; font-size: 18px; font-weight: 600; color: #fff; }
.brand-sub { font-size: 11px; color: rgba(255,255,255,0.7); letter-spacing: 0.5px; }
.header :deep(.n-button) { color: rgba(255,255,255,0.85) !important; }
.header :deep(.n-button:hover) { color: #fff !important; }
.content { display: flex; padding: 24px 28px; gap: 24px; max-width: 1400px; margin: 0 auto; }
.sidebar { flex-shrink: 0; width: 220px; }
.sidebar-title { display: block; font-size: 12px; font-weight: 600; text-transform: uppercase; letter-spacing: 1px; margin-bottom: 8px; padding-left: 4px; }
.main { flex: 1; min-width: 0; }
.search-area { display: flex; gap: 10px; margin-bottom: 16px; }
.search-input { flex: 1; }
.search-btn { width: 100px; }
.result-bar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; font-size: 13px; }
</style>
