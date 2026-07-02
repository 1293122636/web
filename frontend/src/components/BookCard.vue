<template>
  <n-card :bordered="false" hoverable class="book-card" @click="$emit('click')">
    <div class="cover-wrap">
      <img v-if="book.cover" :src="book.cover" class="cover-img" loading="lazy" />
      <div v-else class="cover-placeholder">
        <span class="cover-letter">{{ book.title?.[0] || '?' }}</span>
      </div>
    </div>
    <div class="info">
      <n-ellipsis style="max-width: 100%; line-height: 1.3;">
        <span class="title">{{ book.title }}</span>
      </n-ellipsis>
      <span class="author">{{ book.author }}</span>
      <div class="meta">
        <StatusBadge :status="book.status" />
        <span class="avail">{{ book.available }}/{{ book.total }}可借</span>
      </div>
    </div>
  </n-card>
</template>

<script setup lang="ts">
import { NCard, NEllipsis } from 'naive-ui'
import StatusBadge from './StatusBadge.vue'
import type { BookSummary } from '../types/api'

defineProps<{ book: BookSummary }>()
defineEmits<{ click: [] }>()
</script>

<style scoped>
.book-card {
  cursor: pointer; width: 190px;
  transition: transform 0.2s ease, box-shadow 0.25s ease;
  border-radius: 12px !important;
  overflow: hidden;
}
.book-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 35px rgba(94,106,210,0.12), 0 4px 12px rgba(0,0,0,0.06);
}
.cover-wrap {
  width: 156px; height: 200px; margin: 0 auto 10px;
  border-radius: 8px; overflow: hidden;
}
.cover-img { width: 100%; height: 100%; object-fit: cover; transition: transform 0.3s ease; }
.book-card:hover .cover-img { transform: scale(1.08); }
.cover-placeholder {
  width: 100%; height: 100%;
  display: flex; align-items: center; justify-content: center;
  background: linear-gradient(145deg, #5e6ad2, #7c6fdb, #a78bfa);
  border-radius: 8px;
}
.cover-letter { font-size: 52px; font-weight: 700; color: rgba(255,255,255,0.85); text-shadow: 0 2px 8px rgba(0,0,0,0.2); }
.info { text-align: center; padding: 0 2px; }
.title { font-size: 14px; font-weight: 600; color: var(--n-text-color); line-height: 1.3; }
.author { font-size: 12px; color: var(--n-text-color-3); display: block; margin: 3px 0; line-height: 1.2; }
.meta { display: flex; align-items: center; justify-content: center; gap: 6px; margin-top: 6px; }
.avail { font-size: 11px; color: var(--n-text-color-3); }
</style>
