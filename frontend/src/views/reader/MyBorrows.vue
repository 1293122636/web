<template>
  <div>
    <h1 class="page-title">我的借阅</h1>
    <n-alert v-if="totalFines > 0" type="error" :bordered="false" style="margin-bottom: 20px; border-radius: 10px;">
      <template #header>
        <span style="font-size:15px;font-weight:600">欠费提醒</span>
      </template>
      <n-text depth="2">您有未缴罚款：</n-text>
      <n-text type="error" strong style="font-size:18px">¥{{ totalFines.toFixed(2) }}</n-text>
    </n-alert>
    <n-tabs type="line" animated default-value="borrows" style="margin-bottom: 16px;">
      <n-tab-pane name="borrows" tab="当前借阅">
        <n-data-table
          :columns="columns"
          :data="records"
          :loading="loading"
          :row-key="(r: any) => r.id"
        />
      </n-tab-pane>
      <n-tab-pane name="holds" tab="我的预约">
        <n-data-table
          :columns="holdColumns"
          :data="holds"
          size="small"
          :row-key="(r: any) => r.id"
        />
      </n-tab-pane>
    </n-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, h } from 'vue'
import { useMessage, NTag, NButton, NPopconfirm } from 'naive-ui'
import { useAuthStore } from '@/stores/auth'
import api from '@/api'
import type { DataTableColumn } from 'naive-ui'

const message = useMessage()
const auth = useAuthStore()
const records = ref<any[]>([])
const holds = ref<any[]>([])
const loading = ref(false)
const totalFines = ref(0)

const columns: DataTableColumn[] = [
  { title: '图书', key: 'book.title', ellipsis: { tooltip: true } },
  { title: '作者', key: 'book.author', width: 100 },
  { title: '借阅日', key: 'borrowDate', width: 110, render: (r: any) => new Date(r.borrowDate).toLocaleDateString('zh-CN') },
  { title: '到期日', key: 'dueDate', width: 110, render: (r: any) => new Date(r.dueDate).toLocaleDateString('zh-CN') },
  {
    title: '状态', key: 'status', width: 80,
    render(row: any) {
      const m: Record<string, { type: 'success' | 'warning' | 'error' | 'info' | 'default'; label: string }> = {
        active: { type: 'success', label: '在借' },
        overdue: { type: 'error', label: '逾期' },
        returned: { type: 'default', label: '已还' }
      }
      const s = m[row.status] || { type: 'default' as const, label: row.status }
      return h(NTag, { type: s.type, size: 'small' }, () => s.label)
    }
  },
  {
    title: '操作', key: 'actions', width: 100,
    render(row: any) {
      if (row.status !== 'active') return ''
      return h('div', { style: 'display:flex;gap:6px' }, [
        h(NPopconfirm, { onPositiveClick: () => handleReturn(row.id) }, {
          trigger: () => h(NButton, { size: 'tiny', type: 'warning' }, () => '还书'),
          default: () => '确认还书？'
        }),
        h(NButton, { size: 'tiny', onClick: () => handleRenew(row.id) }, () => '续借')
      ])
    }
  }
]

const holdColumns: DataTableColumn[] = [
  { title: '图书', key: 'book.title', ellipsis: { tooltip: true } },
  { title: '申请时间', key: 'requestDate', width: 160, render: (r: any) => new Date(r.requestDate).toLocaleString('zh-CN') },
  {
    title: '状态', key: 'status', width: 100,
    render(row: any) {
      const m: Record<string, { type: 'success' | 'warning' | 'error' | 'info' | 'default'; label: string }> = {
        pending: { type: 'info', label: '等待中' },
        ready: { type: 'success', label: '可领取' },
        fulfilled: { type: 'default', label: '已完成' },
        cancelled: { type: 'error', label: '已取消' }
      }
      const s = m[row.status] || { type: 'default' as const, label: row.status }
      return h(NTag, { type: s.type, size: 'small' }, () => s.label)
    }
  },
  {
    title: '操作', key: 'actions', width: 80,
    render(row: any) {
      if (row.status !== 'pending') return ''
      return h(NPopconfirm, { onPositiveClick: () => handleCancelHold(row.id) }, {
        trigger: () => h(NButton, { size: 'tiny', type: 'error' }, () => '取消'),
        default: () => '确认取消预约？'
      })
    }
  }
]

async function fetchData() {
  loading.value = true
  try {
    const [bRes, hRes] = await Promise.allSettled([
      api.get('/borrows/my'),
      api.get('/holds/my')
    ])
    if (bRes.status === 'fulfilled') {
      records.value = bRes.value.data.borrows || []
    }
    if (hRes.status === 'fulfilled') {
      holds.value = hRes.value.data || []
    }
    totalFines.value = auth.user?.totalFines || 0
  } catch { /* ignore */ }
  loading.value = false
}

async function handleReturn(id: number) {
  try { await api.post(`/borrows/return/${id}`); message.success('已还书'); fetchData() }
  catch (e: unknown) { message.error((e as Error).message) }
}

async function handleRenew(id: number) {
  try { await api.post(`/borrows/renew/${id}`); message.success('已续借'); fetchData() }
  catch (e: unknown) { message.error((e as Error).message) }
}

async function handleCancelHold(id: number) {
  try { await api.delete(`/holds/${id}`); message.success('已取消预约'); fetchData() }
  catch (e: unknown) { message.error((e as Error).message) }
}

onMounted(() => fetchData())
</script>

<style scoped>
.page-title { font-size: 20px; font-weight: 600; margin: 0 0 20px; }
</style>
