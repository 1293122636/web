<template>
  <div class="facet-panel">
    <h4>聚类条件</h4>
    <n-collapse>
      <n-collapse-item v-for="group in facetGroups" :key="group.key" :title="group.label">
        <div v-for="item in group.items" :key="item.value" class="facet-item"
          :class="{ active: isActive(group.key, item.value) }"
          @click="$emit('select', group.key, item.value)">
          <span class="facet-label">{{ item.value }}</span>
          <n-tag size="tiny" :bordered="false">{{ item.count }}</n-tag>
        </div>
      </n-collapse-item>
    </n-collapse>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { NCollapse, NCollapseItem, NTag } from 'naive-ui'
import type { FacetValue } from '../types/api'

const props = defineProps<{ facets: Record<string, FacetValue[]> | null; active: Record<string, string> }>()
defineEmits<{ select: [key: string, value: string] }>()

const facetLabels: Record<string, string> = {
  campus: '校区', location: '馆藏地', language: '语种', subject: '分类', yearRange: '出版年代'
}

const facetGroups = computed(() => {
  if (!props.facets) return []
  return Object.entries(props.facets).map(([key, items]) => ({
    key, label: facetLabels[key] || key, items: items.slice(0, 10)
  }))
})

function isActive(key: string, value: string) { return props.active[key] === value }
</script>

<style scoped>
.facet-panel { width: 240px; padding: 12px; }
.facet-panel h4 { margin: 0 0 8px; font-size: 15px; }
.facet-item { display: flex; justify-content: space-between; align-items: center; padding: 4px 8px;
  cursor: pointer; border-radius: 4px; font-size: 13px; }
.facet-item:hover { background: var(--n-color-hover); }
.facet-item.active { background: var(--n-color-pressed); }
.facet-label { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
</style>
