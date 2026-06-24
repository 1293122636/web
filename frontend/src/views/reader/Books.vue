<template>
  <div>
    <n-h1 prefix="bar" style="margin-bottom: 20px;"><n-text type="primary">图书浏览</n-text></n-h1>

    <n-space justify="space-between" style="margin-bottom: 16px;">
      <n-space>
        <n-input v-model:value="search" placeholder="搜索书名/作者/ISBN" clearable style="width: 260px;" @keyup.enter="fetchBooks" />
        <n-select v-model:value="filterCategory" placeholder="全部分类" clearable :options="catOptions" style="width: 160px;" @update:value="fetchBooks" />
        <n-button @click="fetchBooks">搜索</n-button>
      </n-space>
    </n-space>

    <n-data-table
      :columns="columns"
      :data="books"
      :loading="loading"
      :pagination="pagination"
      remote
      :row-key="(r: DataRow) => r.id"
      @update:page="onPage"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, h } from 'vue'
import { useMessage, NTag, NButton } from 'naive-ui'
import { api, bookApi } from '../../api'
import type { BookListResponse, BookSummary, CategoryResponse, DataRow } from '../../types/api'
import type { DataTableColumns } from 'naive-ui'

const message = useMessage()
const books = ref<BookSummary[]>([])
const loading = ref(false)
const search = ref('')
const filterCategory = ref<number | null>(null)
const catOptions = ref<{ label: string; value: number }[]>([])
const pagination = reactive({ page: 1, pageSize: 20, itemCount: 0 })

const columns: DataTableColumns<Record<string, unknown>> = [
  { title: '书名', key: 'title', ellipsis: { tooltip: true } },
  { title: '作者', key: 'author', width: 120 },
  { title: '分类', key: 'category.name', width: 100 },
  { title: '出版社', key: 'publisher', width: 140, ellipsis: { tooltip: true } },
  { title: '年份', key: 'year', width: 60 },
  {
    title: '库存', key: 'available', width: 70,
    render: (r) => h(NTag, { type: r.available > 0 ? 'success' : 'error', size: 'small' }, () => `${r.available}/${r.total}`)
  },
  {
    title: '操作', key: 'actions', width: 90,
    render(row) {
      return row.available > 0
        ? h(NButton, { size: 'small', type: 'primary', onClick: () => handleBorrow(row) }, () => '借阅')
        : h(NTag, { type: 'default', size: 'small' }, () => '已借完')
    }
  }
]

async function fetchBooks() {
  loading.value = true
  try {
    const res = await bookApi.list({
      page: pagination.page,
      limit: pagination.pageSize,
      search: search.value || undefined,
      categoryId: filterCategory.value ?? undefined,
    })
    books.value = res.books
    pagination.itemCount = res.total
  } catch {}
  loading.value = false
}

async function fetchCategories() {
  try { catOptions.value = (await api.get('/categories')).map((c: CategoryResponse) => ({ label: c.name, value: c.id })) } catch {}
}

function onPage(page: number) { pagination.page = page; fetchBooks() }

async function handleBorrow(row: DataRow) {
  try {
    await api.post('/borrows/borrow', { bookId: row.id })
    message.success(`已借阅《${row.title}》`)
    fetchBooks()
  } catch (e: unknown) { message.error((e as Error).message) }
}

onMounted(() => { fetchCategories(); fetchBooks() })
</script>
