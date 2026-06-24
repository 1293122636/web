<template>
  <div>
    <n-h1 prefix="bar" style="margin-bottom: 20px;"><n-text type="primary">统计报表</n-text></n-h1>

    <n-grid :cols="5" :x-gap="16" :y-gap="16" responsive="screen" style="margin-bottom: 32px;">
      <n-grid-item v-for="s in statCards" :key="s.label">
        <n-card size="small">
          <n-statistic :label="s.label" :value="s.value" />
        </n-card>
      </n-grid-item>
    </n-grid>

    <n-space vertical :size="24">
      <n-card title="热门图书 Top 20">
        <n-data-table :columns="popularColumns" :data="popularBooks" :loading="loading" size="small" :row-key="(r: any) => r.id" />
      </n-card>

      <n-card title="月度借阅量（近 12 个月）">
        <n-data-table :columns="monthlyColumns" :data="monthlyData" size="small" :row-key="(r: any) => r.month" />
      </n-card>
    </n-space>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { api } from '../../api'
import type { DataTableColumns } from 'naive-ui'

const statCards = ref([
  { label: '总藏书', value: 0 },
  { label: '在借数量', value: 0 },
  { label: '读者总数', value: 0 },
  { label: '分类数', value: 0 },
  { label: '逾期未还', value: 0 }
])

const popularBooks = ref<any[]>([])
const monthlyData = ref<any[]>([])
const loading = ref(false)

const popularColumns: DataTableColumns<any> = [
  { title: '书名', key: 'title', ellipsis: { tooltip: true } },
  { title: '作者', key: 'author', width: 100 },
  { title: '分类', key: 'category.name', width: 100 },
  { title: '借阅次数', key: '_count.borrowRecords', width: 90 }
]

const monthlyColumns: DataTableColumns<any> = [
  { title: '月份', key: 'month', width: 100 },
  { title: '借阅量', key: 'count', width: 100 }
]

onMounted(async () => {
  try {
    const [stats, popular, monthly]: any[] = await Promise.all([
      api.get('/stats'),
      api.get('/stats/popular'),
      api.get('/stats/monthly')
    ])
    if (stats) {
      statCards.value = [
        { label: '总藏书', value: stats.totalBooks || 0 },
        { label: '在借数量', value: stats.activeBorrows || 0 },
        { label: '读者总数', value: stats.totalReaders || 0 },
        { label: '分类数', value: stats.totalCategories || 0 },
        { label: '逾期未还', value: stats.overdueCount || 0 }
      ]
    }
    popularBooks.value = popular || []
    monthlyData.value = monthly || []
  } catch {}
})
</script>
