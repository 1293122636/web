<template>
  <n-tag :type="tagType" size="small" :bordered="false" round>
    {{ label }}
  </n-tag>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { NTag } from 'naive-ui'

const props = defineProps<{ status: string }>()

const statusMap: Record<string, { label: string; type: 'success' | 'warning' | 'info' | 'error' | 'default' }> = {
  available: { label: '在架', type: 'success' },
  borrowed: { label: '借出', type: 'warning' },
  on_hold: { label: '预约中', type: 'info' },
  overdue: { label: '逾期', type: 'error' },
  repairing: { label: '修补中', type: 'default' },
  lost: { label: '遗失', type: 'error' },
  withdrawn: { label: '剔除', type: 'default' },
  removed: { label: '下架', type: 'default' },
}

const entry = computed(() => statusMap[props.status] || { label: props.status, type: 'default' as const })
const label = computed(() => entry.value.label)
const tagType = computed(() => entry.value.type)
</script>
