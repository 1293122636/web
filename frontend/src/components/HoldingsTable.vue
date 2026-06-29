<template>
  <n-data-table :columns="columns" :data="items" :bordered="false" size="small" />
</template>

<script setup lang="ts">
import { h } from 'vue'
import { NDataTable } from 'naive-ui'
import StatusBadge from './StatusBadge.vue'
import type { BookItemSummary } from '../types/api'

defineProps<{ items: BookItemSummary[] }>()

const columns = [
  { title: '序号', key: 'index', width: 60, render: (_: any, __: number) => __ + 1 },
  { title: '索书号', key: 'callNumber', width: 120 },
  { title: '校区', key: 'campus', width: 80 },
  { title: '馆藏地', key: 'location', width: 140 },
  { title: '状态', key: 'status', width: 80, render: (row: BookItemSummary) => h(StatusBadge, { status: row.status }) },
  { title: '请求数', key: 'requests', width: 70 },
]
</script>
