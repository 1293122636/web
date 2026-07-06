<template>
  <div class="facet-panel">
    <n-collapse :default-expanded-names="['campus','subject']">
      <n-collapse-item v-for="group in facetGroups" :key="group.key" :name="group.key">
        <template #header>
          <span class="facet-header">{{ group.label }}</span>
        </template>
        <div v-for="item in group.items" :key="item.value" class="facet-item"
          :class="{ active: isActive(group.key, item.value) }"
          @click="$emit('select', group.key, item.value)">
          <span class="facet-label">{{ item.value }}</span>
          <span class="facet-count">{{ item.count }}</span>
        </div>
      </n-collapse-item>
    </n-collapse>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { NCollapse, NCollapseItem } from 'naive-ui'
import type { FacetValue } from '../types/api'

const props = defineProps<{ facets: Record<string, FacetValue[]> | null; active: Record<string, string> }>()
defineEmits<{ select: [key: string, value: string] }>()

const facetLabels: Record<string, string> = {
  campus: '校区', location: '馆藏地', language: '语种', subject: '分类', yearRange: '出版年代'
}

const facetGroups = computed(() => {
  if (!props.facets) return []
  return Object.entries(props.facets)
    .filter(([, items]) => items && items.length > 0)
    .map(([key, items]) => ({
      key, label: facetLabels[key] || key, items: items.slice(0, 10)
    }))
})

function isActive(key: string, value: string) { return props.active[key] === value }
</script>

<style scoped>
.facet-panel { width: 100%; }
.facet-header { font-size: 13px; font-weight: 600; }
.facet-item {
  display: flex; justify-content: space-between; align-items: center;
  padding: 6px 10px; cursor: pointer; border-radius: 6px;
  font-size: 13px; transition: all 0.12s ease;
}
.facet-item:hover { background: rgba(94,106,210,0.06); }
.facet-item.active { background: rgba(94,106,210,0.1); color: var(--n-primary-color); font-weight: 600; }
.facet-item.active .facet-count { background: rgba(94,106,210,0.15); color: var(--n-primary-color); }
.facet-label { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; flex: 1; }
.facet-count {
  font-size: 11px; padding: 1px 8px; border-radius: 10px;
  background: rgba(0,0,0,0.04); color: var(--n-text-color-3);
  min-width: 28px; text-align: center;
}
</style>
