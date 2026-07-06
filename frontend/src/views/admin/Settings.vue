<template>
  <div>
    <n-h1 prefix="bar" style="margin-bottom: 20px;"><n-text type="primary">系统设置</n-text></n-h1>

    <n-space vertical :size="24">
      <n-card title="借阅规则">
        <n-data-table
          :columns="ruleColumns"
          :data="rules"
          :loading="loading"
          size="small"
          :row-key="(r: any) => r.id"
        />
      </n-card>

      <n-card title="读者类型">
        <n-data-table
          :columns="patronColumns"
          :data="patronCategories"
          size="small"
          :row-key="(r: any) => r.id"
        />
      </n-card>

      <n-card title="资料类型">
        <n-data-table
          :columns="itemTypeColumns"
          :data="itemTypes"
          size="small"
          :row-key="(r: any) => r.id"
        />
      </n-card>
    </n-space>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import type { DataTableColumn } from 'naive-ui'
import api from '@/api'

const rules = ref<any[]>([])
const patronCategories = ref<any[]>([])
const itemTypes = ref<any[]>([])
const loading = ref(false)

const ruleColumns: DataTableColumn[] = [
  { title: '读者类型', key: 'patronCategory.name', width: 120 },
  { title: '资料类型', key: 'itemType.name', width: 120 },
  { title: '借阅上限', key: 'maxBorrows', width: 80 },
  { title: '借阅天数', key: 'loanDays', width: 80 },
  { title: '续借次数', key: 'renewals', width: 80 },
  { title: '续借天数', key: 'renewalDays', width: 80 },
  { title: '日罚金', key: 'finePerDay', width: 80, render: (r: any) => `¥${r.finePerDay}` }
]

const patronColumns: DataTableColumn[] = [
  { title: '名称', key: 'name' }
]

const itemTypeColumns: DataTableColumn[] = [
  { title: '名称', key: 'name' },
  { title: '默认借期', key: 'loanDays', render: (r: any) => `${r.loanDays} 天` },
  { title: '日罚金', key: 'fineRate', render: (r: any) => `¥${r.fineRate}` }
]

onMounted(async () => {
  loading.value = true
  try {
    const [r, p, i] = await Promise.allSettled([
      api.get('/rules'),
      api.get('/rules/patron-categories'),
      api.get('/rules/item-types')
    ])
    rules.value = r.status === 'fulfilled' ? r.value.data : []
    patronCategories.value = p.status === 'fulfilled' ? p.value.data : []
    itemTypes.value = i.status === 'fulfilled' ? i.value.data : []
  } catch (e) { console.error('fetchSettings failed:', e) }
  loading.value = false
})
</script>
