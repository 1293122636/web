<template>
  <div>
    <n-h1 prefix="bar" style="margin-bottom: 20px;"><n-text type="primary">我的借阅</n-text></n-h1>

    <n-alert v-if="totalFines > 0" type="error" :bordered="false" style="margin-bottom: 16px;">
      <n-text depth="2">欠费总额：</n-text><n-text type="error" strong>¥{{ totalFines.toFixed(2) }}</n-text>
    </n-alert>

    <n-data-table
      :columns="columns"
      :data="records"
      :loading="loading"
      :row-key="(r: DataRow) => r.id"
    />

    <!-- Fines Section -->
    <n-card v-if="fines.length > 0" title="欠费明细" style="margin-top: 24px;">
      <n-data-table
        :columns="fineColumns"
        :data="fines"
        size="small"
        :row-key="(r: DataRow) => r.id"
      />
    </n-card>

    <!-- Holds Section -->
    <n-card v-if="holds.length > 0" title="我的预约" style="margin-top: 24px;">
      <n-data-table
        :columns="holdColumns"
        :data="holds"
        size="small"
        :row-key="(r: DataRow) => r.id"
      />
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, h, computed } from 'vue'
import { useMessage, NTag, NButton } from 'naive-ui'
import { api } from '../../api'
import type { BorrowRecordResponse, FineResponse, HoldResponse, DataRow } from '../../types/api'
import type { DataTableColumns } from 'naive-ui'

const message = useMessage()
const records = ref<BorrowRecordResponse[]>([])
const fines = ref<FineResponse[]>([])
const holds = ref<HoldResponse[]>([])
const loading = ref(false)

const totalFines = computed(() =>
  fines.value.reduce((sum: number, f: FineResponse) => sum + (f.paid ? 0 : Number(f.amount)), 0)
)

const statusMap: Record<string, { type: 'success' | 'warning' | 'error' | 'info' | 'default'; label: string }> = {
  active: { type: 'success', label: '在借' },
  returned: { type: 'default', label: '已还' },
  overdue: { type: 'error', label: '逾期' }
}

const columns: DataTableColumns<Record<string, unknown>> = [
  { title: '书名', key: 'book.title', ellipsis: { tooltip: true } },
  { title: 'ISBN', key: 'book.isbn', width: 130 },
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
    title: '操作', key: 'actions', width: 70,
    render(row) {
      if (row.status !== 'active') return ''
      if (row.renewed) return h(NTag, { type: 'warning', size: 'small' }, () => '已续')
      return h(NButton, { size: 'small', text: true, type: 'primary', onClick: () => handleRenew(row.id) }, () => '续借')
    }
  }
]

const fineColumns: DataTableColumns<Record<string, unknown>> = [
  { title: '类型', key: 'type', width: 80, render: (r) => h(NTag, { type: r.type === 'overdue' ? 'error' : 'warning', size: 'tiny' }, () => r.type === 'overdue' ? '逾期' : r.type) },
  { title: '金额', key: 'amount', width: 100, render: (r) => h('span', { style: 'color:#ef4444;' }, `¥${r.amount}`) },
  { title: '状态', key: 'paid', width: 80, render: (r) => h(NTag, { type: r.paid ? 'success' : 'error', size: 'tiny' }, () => r.paid ? '已缴' : '未缴') }
]

const holdColumns: DataTableColumns<Record<string, unknown>> = [
  { title: '书名', key: 'book', width: 200, ellipsis: { tooltip: true },
    render: (r) => (r as unknown as HoldResponse).book?.title || '-' },
  { title: '预约日', key: 'requestDate', width: 110, render: (r) => new Date(r.requestDate).toLocaleDateString('zh-CN') },
  { title: '状态', key: 'status', width: 80, render: (r) => {
    const m: Record<string, { t: string; l: string }> = { pending: { t: 'warning', l: '排队中' }, ready: { t: 'success', l: '可取' }, fulfilled: { t: 'default', l: '已取' }, cancelled: { t: 'error', l: '已取消' }, expired: { t: 'default', l: '已过期' } }
    const s = m[r.status] || { t: 'default', l: r.status }
    return h(NTag, { type: s.t as any, size: 'tiny' }, () => s.l)
  }},
  { title: '操作', key: 'actions', width: 70, render: (r) => {
    if (r.status !== 'pending') return ''
    return h(NButton, { size: 'small', text: true, type: 'error', onClick: () => handleCancelHold(r.id) }, () => '取消')
  }}
]

async function fetchRecords() {
  loading.value = true
  try { records.value = (await api.get<{ borrows: any[]; total: number }>('/borrows/my')).borrows } catch {}
  loading.value = false
}

async function fetchFines() {
  try { fines.value = await api.get('/fines/my') } catch {}
}

async function handleRenew(id: number) {
  try { await api.post(`/borrows/renew/${id}`); message.success('续借成功'); fetchRecords() }
  catch (e: unknown) { message.error((e as Error).message) }
}

async function fetchHolds() {
  try { holds.value = await api.get<HoldResponse[]>('/holds/my') } catch {}
}

async function handleCancelHold(id: number) {
  try { await api.delete(`/holds/${id}`); message.success('已取消预约'); fetchHolds() }
  catch (e: unknown) { message.error((e as Error).message) }
}

onMounted(() => { fetchRecords(); fetchFines(); fetchHolds() })
</script>
