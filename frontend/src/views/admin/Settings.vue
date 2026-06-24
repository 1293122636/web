<template>
  <div>
    <n-h1 prefix="bar" style="margin-bottom: 20px;"><n-text type="primary">系统设置</n-text></n-h1>

    <n-space vertical :size="24">
      <!-- Circulation Rules -->
      <n-card title="借阅规则">
        <n-data-table
          :columns="ruleColumns"
          :data="rules"
          :loading="loading"
          size="small"
          :row-key="(r: DataRow) => r.id"
        />
      </n-card>

      <!-- Patron Categories -->
      <n-card title="读者类型">
        <n-data-table
          :columns="patronColumns"
          :data="patronCategories"
          size="small"
          :row-key="(r: DataRow) => r.id"
        />
      </n-card>

      <!-- Item Types -->
      <n-card title="资料类型">
        <n-data-table
          :columns="itemTypeColumns"
          :data="itemTypes"
          size="small"
          :row-key="(r: DataRow) => r.id"
        />
      </n-card>
    </n-space>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import type { DataTableColumns } from 'naive-ui'
import { api } from '../../api'
import type { CirculationRuleResponse, PatronCategoryResponse, ItemTypeResponse } from '../../types/api'

const rules = ref<CirculationRuleResponse[]>([])
const patronCategories = ref<PatronCategoryResponse[]>([])
const itemTypes = ref<ItemTypeResponse[]>([])
const loading = ref(false)

const ruleColumns: DataTableColumns<Record<string, unknown>> = [
  { title: '读者类型', key: 'patronCategory.name', width: 120 },
  { title: '资料类型', key: 'itemType.name', width: 120 },
  { title: '借阅上限', key: 'maxBorrows', width: 80 },
  { title: '借阅天数', key: 'loanDays', width: 80 },
  { title: '续借次数', key: 'renewals', width: 80 },
  { title: '续借天数', key: 'renewalDays', width: 80 },
  { title: '日罚金', key: 'finePerDay', width: 80, render: (r) => `¥${r.finePerDay}` }
]

const patronColumns: DataTableColumns<Record<string, unknown>> = [
  { title: '名称', key: 'name' }
]

const itemTypeColumns: DataTableColumns<Record<string, unknown>> = [
  { title: '名称', key: 'name' },
  { title: '默认借期', key: 'loanDays', render: (r) => `${r.loanDays} 天` },
  { title: '日罚金', key: 'fineRate', render: (r) => `¥${r.fineRate}` }
]

onMounted(async () => {
  loading.value = true
  try {
    const [r, p, i] = await Promise.all([
      api.get('/rules'),
      api.get('/rules/patron-categories'),
      api.get('/rules/item-types')
    ])
    rules.value = r
    patronCategories.value = p
    itemTypes.value = i
  } catch {}
  loading.value = false
})
</script>
