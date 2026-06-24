<template>
  <div v-if="loading" class="book-grid">
    <SkeletonCard v-for="n in 6" :key="n" />
  </div>
  <div v-else class="book-grid">
    <BookCard v-for="book in books" :key="book.id" :book="book" @click="$emit('select', book.id)" />
  </div>
  <EmptyState v-if="!loading && books.length === 0" message="未找到匹配的图书" />
</template>

<script setup lang="ts">
import BookCard from './BookCard.vue'
import EmptyState from './EmptyState.vue'
import SkeletonCard from './SkeletonCard.vue'
import type { BookSummary } from '../types/api'

defineProps<{ books: BookSummary[]; loading?: boolean }>()
defineEmits<{ select: [id: number] }>()
</script>

<style scoped>
.book-grid { display: flex; flex-wrap: wrap; gap: 16px; }
</style>
