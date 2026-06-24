<template>
  <div>
    <n-h1 prefix="bar" style="margin-bottom: 20px;"><n-text type="primary">借阅管理</n-text></n-h1>

    <n-data-table
      :columns="columns"
      :data="records"
      :loading="loading"
      :row-key="(r: DataRow) => r.id"
    >
      <template #empty><n-empty description="暂无借阅记录" /></template>
    </n-data-table>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, h } from 'vue'
import { useMessage, NTag, NButton, NPopconfirm } from 'naive-ui'
import { api } from '../../api'
import type { BorrowRecordResponse, FineResponse, DataRow } from '../../types/api'
import type { DataTableColumns } from 'naive-ui'

const message = useMessage()
const records = ref<BorrowRecordResponse[]>([])
const loading = ref(false)

const statusMap: Record<string, { type: 'success' | 'warning' | 'error' | 'info' | 'default'; label: string }> = {
  active: { type: 'success', label: '在借' },
  returned: { type: 'default', label: '已还' },
  overdue: { type: 'error', label: '逾期' }
}

const columns: DataTableColumns<Record<string, unknown>> = [
  { title: '读者', key: 'user.name', width: 100 },
  { title: '书名', key: 'book.title', ellipsis: { tooltip: true } },
  { title: '条码', key: 'bookItem.barcode', width: 130, render: (r) => r.bookItem?.barcode || '-' },
  { title: '借阅日', key: 'borrowDate', width: 110, render: (r) => new Date(r.borrowDate).toLocaleDateString('zh-CN') },
  { title: '到期日', key: 'dueDate', width: 110, render: (r) => new Date(r.dueDate).toLocaleDateString('zh-CN') },
  { title: '归还日', key: 'returnDate', width: 110, render: (r) => r.returnDate ? new Date(r.returnDate).toLocaleDateString('zh-CN') : '-' },
  {
    title: '状态', key: 'status', width: 70,
    render(row) {
      const s = statusMap[row.status] || { type: 'default', label: row.status }
      return h(NTag, { type: s.type, size: 'small' }, () => s.label)
    }
  },
  {
    title: '罚款', key: 'fines', width: 90,
    render(row) {
      if (!row.fines || row.fines.length === 0) return ''
      const total = row.fines.reduce((sum: number, f: FineResponse) => sum + (f.paid ? 0 : f.amount), 0)
      return h(NTag, { type: 'error', size: 'small' }, () => `¥${total}`)
    }
  },
  {
    title: '操作', key: 'actions', width: 80,
    render(row) {
      if (row.status !== 'active') return ''
      return h(NPopconfirm, { onPositiveClick: () => handleReturn(row.id) }, {
        trigger: () => h(NButton, { size: 'small' }, () => '还书'),
        default: () => {
          const isOverdue = new Date(row.dueDate) < new Date()
          if (isOverdue) return h('span', {}, [h('span', { style: 'color:#ef4444;' }, '⚠ 此借阅已逾期'), h('br'), '确认还书？将会自动计算逾期罚金。'])
          return '确认还书？'
        }
      })
    }
  }
]

async function fetchRecords() {
  loading.value = true
  try { records.value = (await api.get<{ borrows: BorrowRecordResponse[]; total: number }>('/borrows')).borrows } catch (e) { console.error('fetchRecords failed:', e) }
  loading.value = false
}

async function handleReturn(id: number) {
  try { await api.post(`/borrows/return/${id}`); message.success('已还书'); fetchRecords() }
  catch (e: unknown) { message.error((e as Error).message) }
}

onMounted(() => fetchRecords())
</script>
