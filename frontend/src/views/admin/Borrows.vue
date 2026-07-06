<template>
  <div>
    <n-h1 prefix="bar" style="margin-bottom: 20px;"><n-text type="primary">借阅管理</n-text></n-h1>

    <n-data-table
      :columns="columns"
      :data="records"
      :loading="loading"
      :row-key="(r: any) => r.id"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, h } from 'vue'
import { useMessage, NTag, NButton, NPopconfirm } from 'naive-ui'
import api from '@/api'
import type { DataTableColumn } from 'naive-ui'

const message = useMessage()
const records = ref<any[]>([])
const loading = ref(false)

const statusMap: Record<string, { type: 'success' | 'warning' | 'error' | 'info' | 'default'; label: string }> = {
  active: { type: 'success', label: '在借' },
  returned: { type: 'default', label: '已还' },
  overdue: { type: 'error', label: '逾期' }
}

const columns: DataTableColumn[] = [
  { title: '读者', key: 'user.name', width: 100 },
  { title: '书名', key: 'book.title', ellipsis: { tooltip: true } },
  { title: '条码', key: 'bookItem.barcode', width: 130, render: (r: any) => r.bookItem?.barcode || '-' },
  { title: '借阅日', key: 'borrowDate', width: 110, render: (r: any) => new Date(r.borrowDate).toLocaleDateString('zh-CN') },
  { title: '到期日', key: 'dueDate', width: 110, render: (r: any) => new Date(r.dueDate).toLocaleDateString('zh-CN') },
  { title: '归还日', key: 'returnDate', width: 110, render: (r: any) => r.returnDate ? new Date(r.returnDate).toLocaleDateString('zh-CN') : '-' },
  {
    title: '状态', key: 'status', width: 70,
    render(row: any) {
      const s = statusMap[row.status] || { type: 'default' as const, label: row.status }
      return h(NTag, { type: s.type, size: 'small' }, () => s.label)
    }
  },
  {
    title: '罚款', key: 'fines', width: 90,
    render(row: any) {
      if (!row.fines || row.fines.length === 0) return ''
      const total = row.fines.reduce((sum: number, f: any) => sum + (f.paid ? 0 : f.amount), 0)
      return h(NTag, { type: 'error', size: 'small' }, () => `¥${total}`)
    }
  },
  {
    title: '操作', key: 'actions', width: 80,
    render(row: any) {
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
  try {
    const { data } = await api.get('/borrows')
    records.value = data.borrows || []
  } catch { /* ignore */ }
  loading.value = false
}

async function handleReturn(id: number) {
  try { await api.post(`/borrows/return/${id}`); message.success('已还书'); fetchRecords() }
  catch (e: unknown) { message.error((e as Error).message) }
}

onMounted(() => fetchRecords())
</script>
