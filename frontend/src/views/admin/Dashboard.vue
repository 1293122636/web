<template>
  <div>
    <n-h1 prefix="bar" style="margin-bottom: 24px;">
      <n-text type="primary">管理概览</n-text>
    </n-h1>

    <n-grid :cols="5" :x-gap="16" :y-gap="16" responsive="screen" style="margin-bottom: 32px;">
      <n-grid-item v-for="s in stats" :key="s.label">
        <n-card size="small">
          <n-statistic :label="s.label" :value="s.value" />
        </n-card>
      </n-grid-item>
    </n-grid>

    <n-card title="借阅动态" style="margin-top: 16px;">
      <n-empty description="暂无借阅数据" style="padding: 32px 0;" />
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { api } from '../../api'
import type { StatsOverviewResponse } from '../../types/api'

const stats = ref([
  { label: '总藏书', value: '-' },
  { label: '在借数量', value: '-' },
  { label: '读者总数', value: '-' },
  { label: '分类数', value: '-' },
  { label: '逾期未还', value: '-' }
])

onMounted(async () => {
  try {
    const res = await api.get<StatsOverviewResponse>('/stats')
    stats.value = [
      { label: '总藏书', value: res.totalBooks || 0 },
      { label: '在借数量', value: res.activeBorrows || 0 },
      { label: '读者总数', value: res.totalReaders || 0 },
      { label: '分类数', value: res.totalCategories || 0 },
      { label: '逾期未还', value: res.overdueCount || 0 }
    ]
  } catch (e) { console.error('fetchStats failed:', e) }
})
</script>
