<template>
  <div>
    <n-h1 prefix="bar" style="margin-bottom: 20px;"><n-text type="primary">图书管理</n-text></n-h1>

    <n-space justify="space-between" style="margin-bottom: 16px;">
      <n-space>
        <n-input v-model:value="search" placeholder="搜索书名/作者/ISBN" clearable style="width: 260px;" @keyup.enter="fetchBooks" />
        <n-select v-model:value="filterCategory" placeholder="全部分类" clearable :options="catOptions" style="width: 160px;" @update:value="fetchBooks" />
        <n-button @click="fetchBooks">搜索</n-button>
      </n-space>
      <n-button type="primary" @click="openCreate">添加图书</n-button>
    </n-space>

    <n-data-table
      :columns="columns"
      :data="books"
      :loading="loading"
      :pagination="pagination"
      remote
      :row-key="(r: DataRow) => r.id"
      :expanded-row-keys="expandedKeys"
      @update:expanded-row-keys="onExpand"
      @update:page="onPage"
    />

    <!-- Modal: Create / Edit Book -->
    <n-modal v-model:show="showModal" :title="editingId ? '编辑图书' : '添加图书'" preset="card" style="width: 560px;">
      <n-form ref="formRef" :model="form" :rules="rulesData" label-placement="top">
        <n-grid :cols="2" :x-gap="16">
          <n-grid-item><n-form-item label="ISBN" path="isbn"><n-input v-model:value="form.isbn" /></n-form-item></n-grid-item>
          <n-grid-item><n-form-item label="书名" path="title"><n-input v-model:value="form.title" /></n-form-item></n-grid-item>
          <n-grid-item><n-form-item label="作者" path="author"><n-input v-model:value="form.author" /></n-form-item></n-grid-item>
          <n-grid-item><n-form-item label="出版社" path="publisher"><n-input v-model:value="form.publisher" /></n-form-item></n-grid-item>
          <n-grid-item><n-form-item label="年份" path="year"><n-input-number v-model:value="form.year" :min="1900" :max="2099" style="width:100%" /></n-form-item></n-grid-item>
          <n-grid-item><n-form-item label="分类" path="categoryId"><n-select v-model:value="form.categoryId" :options="catOptions" /></n-form-item></n-grid-item>
          <n-grid-item><n-form-item label="数量" path="total"><n-input-number v-model:value="form.total" :min="1" style="width:100%" /></n-form-item></n-grid-item>
          <n-grid-item><n-form-item label="位置"><n-input v-model:value="form.location" placeholder="书架位置" /></n-form-item></n-grid-item>
        </n-grid>
        <n-form-item label="描述"><n-input v-model:value="form.desc" type="textarea" placeholder="图书简介" /></n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showModal = false">取消</n-button>
          <n-button type="primary" :loading="saving" @click="handleSave">{{ editingId ? '保存' : '添加' }}</n-button>
        </n-space>
      </template>
    </n-modal>

    <!-- Modal: Add Copy -->
    <n-modal v-model:show="showCopyModal" title="添加复本" preset="card" style="width: 480px;">
      <n-form :model="copyForm" label-placement="top">
        <n-form-item label="条码号" required><n-input v-model:value="copyForm.barcode" placeholder="LIB-000001-4" /></n-form-item>
        <n-form-item label="索书号"><n-input v-model:value="copyForm.callNumber" placeholder="TP312/1002" /></n-form-item>
        <n-form-item label="馆藏地"><n-input v-model:value="copyForm.location" placeholder="总馆A区3楼" /></n-form-item>
        <n-form-item label="价格"><n-input-number v-model:value="copyForm.price" :min="0" :step="0.01" style="width: 100%" /></n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showCopyModal = false">取消</n-button>
          <n-button type="primary" :loading="copySaving" @click="handleAddCopy">添加</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, h } from 'vue'
import { useMessage, NTag, NButton, NPopconfirm, NPopover } from 'naive-ui'
import { api, bookApi } from '../../api'
import type { BookListResponse, BookItemSummary, BookSummary, CategoryResponse, DataRow } from '../../types/api'
import type { DataTableColumns } from 'naive-ui'

const message = useMessage()
const books = ref<BookSummary[]>([])
const loading = ref(false)
const search = ref('')
const filterCategory = ref<number | null>(null)
const catOptions = ref<{ label: string; value: number }[]>([])
const pagination = reactive({ page: 1, pageSize: 20, itemCount: 0 })
const expandedKeys = ref<number[]>([])

const columns: DataTableColumns<Record<string, unknown>> = [
  { type: 'expand', renderExpand: (row) => h(ExpandItems, { bookId: row.id, onRefresh: fetchBooks }) },
  { title: 'ISBN', key: 'isbn', width: 140 },
  { title: '书名', key: 'title', ellipsis: { tooltip: true } },
  { title: '作者', key: 'author', width: 120 },
  { title: '分类', key: 'category.name', width: 100 },
  {
    title: '状态', key: 'status', width: 80,
    render(row) {
      const m: Record<string, { type: 'success' | 'warning' | 'error' | 'info' | 'default'; label: string }> = {
        available: { type: 'success', label: '在架' },
        borrowed: { type: 'warning', label: '借出' },
        removed: { type: 'default', label: '下架' }
      }
      const s = m[row.status] || m.available
      return h(NTag, { type: s.type, size: 'small' }, () => s.label)
    }
  },
  { title: '库存', key: 'available', width: 60, render: (r) => `${r.available}/${r.total}` },
  {
    title: '操作', key: 'actions', width: 180,
    render(row) {
      return h('div', { style: 'display:flex;gap:6px' }, [
        h(NButton, { size: 'small', onClick: () => openEdit(row) }, () => '编辑'),
        h(NButton, { size: 'small', onClick: () => openAddCopy(row) }, () => '加复本'),
        h(NPopconfirm, { onPositiveClick: () => handleDelete(row.id) }, {
          trigger: () => h(NButton, { size: 'small', type: 'error', text: true }, () => '删除'),
          default: () => '确认删除该图书？'
        })
      ])
    }
  }
]

// Expand panel for items
const ExpandItems = {
  props: { bookId: Number },
  emits: ['refresh'],
  setup(_props: Record<string, unknown>, { emit }: Record<string, unknown>) {
    const items = ref<any[]>([])
    const load = async () => {
      try {
        const res = await api.get<BookItemsResponse>(`/books/${_props.bookId as number}/items`)
        items.value = res.items || []
      } catch {}
    }
    load()
    return () => {
      if (items.value.length === 0) return h('div', { style: 'padding:12px;color:#8a8f98;' }, '暂无复本')
      return h('div', { style: 'padding:8px 0;' }, [
        h('table', { style: 'width:100%;border-collapse:collapse;font-size:13px;' }, [
          h('thead', {}, h('tr', {}, [
            h('th', { style: 'text-align:left;padding:4px 12px;color:#8a8f98;' }, '条码号'),
            h('th', { style: 'text-align:left;padding:4px 12px;color:#8a8f98;' }, '索书号'),
            h('th', { style: 'text-align:left;padding:4px 12px;color:#8a8f98;' }, '馆藏地'),
            h('th', { style: 'text-align:left;padding:4px 12px;color:#8a8f98;' }, '状态'),
            h('th', { style: 'text-align:left;padding:4px 12px;color:#8a8f98;' }, '品相'),
            h('th', { style: 'text-align:right;padding:4px 12px;color:#8a8f98;' }, '价格')
          ])),
          h('tbody', {}, items.value.map((i: BookItemSummary) => {
            const statusColors: Record<string, any> = { available: 'success', borrowed: 'warning', repairing: 'info', lost: 'error', withdrawn: 'default' }
            return h('tr', { style: 'border-top:1px solid rgba(255,255,255,0.05);' }, [
              h('td', { style: 'padding:6px 12px;font-family:monospace;' }, i.barcode),
              h('td', { style: 'padding:6px 12px;color:#d0d6e0;' }, i.callNumber || '-'),
              h('td', { style: 'padding:6px 12px;color:#d0d6e0;' }, i.location || '-'),
              h('td', { style: 'padding:6px 12px;' }, h(NTag, { type: statusColors[i.status] || 'default', size: 'tiny' }, () => {
                const labels: Record<string, string> = { available: '在架', borrowed: '借出', repairing: '修补', lost: '遗失', withdrawn: '剔除' }
                return labels[i.status] || i.status
              })),
              h('td', { style: 'padding:6px 12px;color:#8a8f98;' }, ({ normal: '正常', damaged: '破损', repairing: '修补中', lost: '遗失', withdrawn: '剔除' } as any)[i.condition] || i.condition),
              h('td', { style: 'padding:6px 12px;text-align:right;color:#d0d6e0;' }, i.price ? `¥${i.price}` : '-')
            ])
          }))
        ])
      ])
    }
  }
}

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
  } catch { message.error('加载失败') }
  loading.value = false
}

async function fetchCategories() {
  try { catOptions.value = (await api.get('/categories')).map((c: CategoryResponse) => ({ label: c.name, value: c.id })) } catch {}
}

function onPage(page: number) { pagination.page = page; fetchBooks() }
function onExpand(keys: number[]) { expandedKeys.value = keys }

// Book Modal
const showModal = ref(false)
const editingId = ref<number | null>(null)
const saving = ref(false)
const form = reactive({ isbn: '', title: '', author: '', publisher: '', year: undefined as number | undefined, categoryId: null as number | null, total: 1, location: '', desc: '' })
const rulesData = {
  isbn: [{ required: true, message: '必填' }],
  title: [{ required: true, message: '必填' }],
  author: [{ required: true, message: '必填' }],
  categoryId: [{ required: true, type: 'number' as const, message: '必选' }]
}

function openCreate() { editingId.value = null; Object.assign(form, { isbn: '', title: '', author: '', publisher: '', year: undefined, categoryId: null, total: 1, location: '', desc: '' }); showModal.value = true }
function openEdit(row: DataRow) { editingId.value = row.id; Object.assign(form, { isbn: row.isbn, title: row.title, author: row.author, publisher: row.publisher, year: row.year, categoryId: row.categoryId, total: row.total, location: row.location, desc: row.desc }); showModal.value = true }

async function handleSave() {
  saving.value = true
  try {
    const data: Record<string, unknown> = { ...form }
    if (editingId.value) { await api.put(`/books/${editingId.value}`, data); message.success('已更新') }
    else { await api.post('/books', data); message.success('已添加') }
    showModal.value = false
    fetchBooks()
  } catch (e: unknown) { message.error((e as Error).message) }
  saving.value = false
}

async function handleDelete(id: number) {
  try { await api.delete(`/books/${id}`); message.success('已删除'); fetchBooks() }
  catch (e: unknown) { message.error((e as Error).message) }
}

// Copy Modal
const showCopyModal = ref(false)
const copyBookId = ref<number | null>(null)
const copySaving = ref(false)
const copyForm = reactive({ barcode: '', callNumber: '', location: '', price: null as number | null })

function openAddCopy(row: DataRow) { copyBookId.value = row.id; copyForm.barcode = ''; copyForm.callNumber = ''; copyForm.location = ''; copyForm.price = null; showCopyModal.value = true }

async function handleAddCopy() {
  if (!copyForm.barcode) { message.warning('条码号必填'); return }
  copySaving.value = true
  try {
    // [DESIGN-TODO: 复本添加后端接口] — 当前无 /api/books/:id/items POST 端点
    message.info('复本功能暂未开放，请联系管理员手动添加')
    showCopyModal.value = false
  } catch (e: unknown) { message.error((e as Error).message) }
  copySaving.value = false
}

onMounted(() => { fetchCategories(); fetchBooks() })
</script>
