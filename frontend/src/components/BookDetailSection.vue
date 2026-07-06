<template>
  <n-card size="small">
    <n-skeleton v-if="loading" text :repeat="3" />
    <n-spin :show="loading">
      <n-tabs type="line" :default-value="defaultTab || 'info'">
        <n-tab-pane name="info" tab="基本信息">
          <n-descriptions :column="2" bordered size="small">
            <n-descriptions-item v-for="f in infoFields" :key="f.label" :label="f.label">{{ f.value }}</n-descriptions-item>
          </n-descriptions>
        </n-tab-pane>
        <n-tab-pane name="holdings" tab="馆藏信息" v-if="items && items.length > 0">
          <HoldingsTable :items="items" />
        </n-tab-pane>
      </n-tabs>
    </n-spin>
  </n-card>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { NCard, NSkeleton, NSpin, NTabs, NTabPane, NDescriptions, NDescriptionsItem } from 'naive-ui'
import HoldingsTable from './HoldingsTable.vue'
import type { BookDetail, BookItemSummary } from '../types/api'

const props = defineProps<{ book: BookDetail | null; items: BookItemSummary[]; loading: boolean; defaultTab?: string }>()

const infoFields = computed(() => {
  if (!props.book) return []
  return [
    { label: '题名/责任者', value: `${props.book.title} / ${props.book.author}` },
    { label: '出版发行', value: [props.book.publisher, props.book.year].filter(Boolean).join(', ') },
    { label: 'ISBN', value: props.book.isbn },
    { label: '中图法分类号', value: props.book.clcNumber || '-' },
    { label: '载体形态', value: props.book.physicalDesc || '-' },
    { label: '语种', value: props.book.language || '-' },
    { label: '馆藏', value: `${props.book.total} 册 (${props.book.available} 可借)` },
  ]
})
</script>
