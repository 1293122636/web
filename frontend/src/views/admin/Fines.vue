<template>
  <div>
    <n-h1 prefix="bar" style="margin-bottom: 20px;"><n-text type="primary">罚款管理</n-text></n-h1>

    <n-space style="margin-bottom: 16px;">
      <n-select v-model:value="filterType" placeholder="类型" clearable :options="typeOptions" style="width: 120px;" @update:value="fetchFines" />
      <n-select v-model:value="filterPaid" placeholder="状态" clearable :options="paidOptions" style="width: 120px;" @update:value="fetchFines" />
      <n-button @click="fetchFines">刷新</n-button>
    </n-space>

    <n-data-table
      :columns="columns"
      :data="fines"
      :loading="loading"
      :row-key="(r: DataRow) => r.id"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, h } from 'vue'
import { useMessage, NTag, NButton } from 'naive-ui'
import { api } from '../../api'
import type { FineResponse, DataRow } from '../../types/api'
import type { DataTableColumns } from 'naive-ui'

const message = useMessage()
const fines = ref<FineResponse[]>([])
const loading = ref(false)
const filterType = ref<string | null>(null)
const filterPaid = ref<string | null>(null)

const typeOptions = [
  { label: '逾期', value: 'overdue' },
  { label: '遗失', value: 'lost' },
  { label: '破损', value: 'damage' }
]
const paidOptions = [
  { label: '未缴', value: 'false' },
  { label: '已缴', value: 'true' }
]

const columns: DataTableColumns<Record<string, unknown>> = [
  { title: '读者', key: 'user.name', width: 100 },
  { title: '图书', key: 'borrowRecord.book.title', ellipsis: { tooltip: true } },
  { title: '金额', key: 'amount', width: 100, render: (r) => `¥${r.amount}` },
  {
    title: '类型', key: 'type', width: 70,
    render(row) {
      const m: Record<string, { type: 'success' | 'warning' | 'error' | 'info' | 'default'; label: string }> = {
        overdue: { type: 'error', label: '逾期' },
        lost: { type: 'warning', label: '遗失' },
        damage: { type: 'info', label: '破损' }
      }
      const s = m[row.type] || { type: 'default', label: row.type }
      return h(NTag, { type: s.type, size: 'small' }, () => s.label)
    }
  },
  {
    title: '状态', key: 'paid', width: 70,
    render(row) {
      return h(NTag, { type: row.paid ? 'success' : 'error', size: 'small' }, () => row.paid ? '已缴' : '未缴')
    }
  },
  { title: '创建时间', key: 'createdAt', width: 160, render: (r) => new Date(r.createdAt).toLocaleString('zh-CN') },
  { title: '缴费时间', key: 'paidAt', width: 160, render: (r) => r.paidAt ? new Date(r.paidAt).toLocaleString('zh-CN') : '-' },
  {
    title: '操作', key: 'actions', width: 80,
    render(row) {
      if (row.paid) return ''
      return h(NButton, { size: 'small', type: 'success', onClick: () => handlePay(row.id) }, () => '缴费')
    }
  }
]

async function fetchFines() {
  loading.value = true
  try {
    const params = new URLSearchParams()
    if (filterType.value) params.set('type', filterType.value)
    if (filterPaid.value) params.set('paid', filterPaid.value)
    fines.value = (await api.get<{ fines: FineResponse[]; total: number }>(`/fines?${params}`)).fines
  } catch {}
  loading.value = false
}

async function handlePay(id: number) {
  try { await api.post(`/fines/${id}/pay`); message.success('已确认缴费'); fetchFines() }
  catch (e: unknown) { message.error((e as Error).message) }
}

onMounted(() => fetchFines())
</script>
