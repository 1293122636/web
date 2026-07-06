<template>
  <div>
    <h1 class="page-title">统计报表</h1>

    <n-grid :cols="5" :x-gap="16" :y-gap="16" responsive="screen" style="margin-bottom: 28px;">
      <n-grid-item v-for="(s, i) in statCards" :key="s.label">
        <n-card size="small" :style="{ borderTop: `3px solid ${colors[i]}` }">
          <n-statistic :label="s.label" :value="s.value">
            <template #prefix><n-icon :size="20" :color="colors[i]"><component :is="icons[i]" /></n-icon></template>
          </n-statistic>
        </n-card>
      </n-grid-item>
    </n-grid>

    <n-space vertical :size="20">
      <n-card title="热门图书 Top 20" size="small" :bordered="false">
        <template #header-extra><n-tag size="small" type="info">借阅次数排行</n-tag></template>
        <n-data-table :columns="popularColumns" :data="popularBooks" :loading="loading" size="small" :row-key="(r: any) => r.id" />
      </n-card>

      <n-card title="月度借阅量（近 12 个月）" size="small" :bordered="false">
        <template #header-extra><n-tag size="small">统计</n-tag></template>
        <n-data-table :columns="monthlyColumns" :data="monthlyData" size="small" :row-key="(r: any) => r.month" />
      </n-card>
    </n-space>
  </div>
</template>

<script setup lang="ts">
import { h, ref, onMounted } from 'vue'
import { NIcon } from 'naive-ui'
import { LibraryOutline, SwapHorizontalOutline, PeopleOutline, GridOutline, AlertCircleOutline } from '@vicons/ionicons5'
import api from '@/api'
import type { DataTableColumn } from 'naive-ui'

const colors = ['#5e6ad2', '#f0a020', '#18a058', '#2080f0', '#d03050']
const icons = [LibraryOutline, SwapHorizontalOutline, PeopleOutline, GridOutline, AlertCircleOutline]

const statCards = ref([
  { label: '总藏书', value: 0 }, { label: '在借数量', value: 0 },
  { label: '读者总数', value: 0 }, { label: '分类数', value: 0 },
  { label: '逾期未还', value: 0 }
])

const popularBooks = ref<any[]>([])
const monthlyData = ref<any[]>([])
const loading = ref(false)

const popularColumns: DataTableColumn[] = [
  { title: '#', key: 'index', width: 50, render: (_: any, i: number) => i + 1 },
  { title: '书名', key: 'title', ellipsis: { tooltip: true } },
  { title: '作者', key: 'author', width: 120 },
  { title: '分类', key: 'category.name', width: 100 },
  { title: '借阅次数', key: '_count.borrowRecords', width: 90, render: (r: any) => h('span', { style: 'font-weight:600;color:#5e6ad2' }, r._count?.borrowRecords || 0) }
]

const monthlyColumns: DataTableColumn[] = [
  { title: '月份', key: 'month', width: 120 },
  { title: '借阅量', key: 'count', width: 100, render: (r: any) => h('span', { style: 'font-weight:600' }, r.count) }
]

onMounted(async () => {
  loading.value = true
  try {
    const [statsRes, popularRes, monthlyRes] = await Promise.allSettled([
      api.get('/stats'),
      api.get('/stats/popular'),
      api.get('/stats/monthly')
    ])
    if (statsRes.status === 'fulfilled') {
      const stats = statsRes.value.data
      if (stats) {
        statCards.value = [
          { label: '总藏书', value: stats.totalBooks || 0 },
          { label: '在借数量', value: stats.activeBorrows || 0 },
          { label: '读者总数', value: stats.totalReaders || 0 },
          { label: '分类数', value: stats.totalCategories || 0 },
          { label: '逾期未还', value: stats.overdueCount || 0 }
        ]
      }
    }
    popularBooks.value = popularRes.status === 'fulfilled' ? (popularRes.value.data || []) : []
    monthlyData.value = monthlyRes.status === 'fulfilled' ? (monthlyRes.value.data || []) : []
  } catch (e) { console.error('fetchStats failed:', e) }
  loading.value = false
})
</script>

<style scoped>
.page-title { font-size: 20px; font-weight: 600; margin: 0 0 20px; }
</style>
