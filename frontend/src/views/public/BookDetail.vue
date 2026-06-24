<template>
  <n-layout class="detail-page">
    <n-layout-header bordered class="header">
      <div class="header-inner">
        <n-button text @click="$router.push('/books')">← 返回搜索</n-button>
        <n-space>
          <n-button text @click="$router.push('/login')">登录</n-button>
        </n-space>
      </div>
    </n-layout-header>
    <n-layout-content>
      <n-spin :show="loading">
        <div v-if="book" class="content">
          <div class="cover-section">
            <img v-if="book.cover" :src="book.cover" class="cover-img" />
            <div v-else class="cover-placeholder">{{ book.title[0] }}</div>
            <n-button type="primary" size="large" style="margin-top:16px" @click="handleBorrow">
              {{ book.available > 0 ? '登录后借阅' : '登录后预约' }}
            </n-button>
            <n-button v-if="book.total > 0 && book.available === 0" type="warning" size="small" style="margin-top:8px" @click="handleHold">
              预约此书 ({{ holdCount }} 人排队)
            </n-button>
          </div>
          <div class="info-section">
            <h2>{{ book.title }}</h2>
            <n-descriptions :column="1" bordered size="small" class="desc-table">
              <n-descriptions-item label="作者">{{ book.author }}</n-descriptions-item>
              <n-descriptions-item label="出版社">{{ book.publisher || '-' }} {{ book.year || '' }}</n-descriptions-item>
              <n-descriptions-item label="ISBN">{{ book.isbn }}</n-descriptions-item>
              <n-descriptions-item label="中图法分类号">{{ book.clcNumber || '-' }}</n-descriptions-item>
              <n-descriptions-item label="载体形态">{{ book.physicalDesc || '-' }}</n-descriptions-item>
              <n-descriptions-item label="语种">{{ book.language || '-' }}</n-descriptions-item>
              <n-descriptions-item label="馆藏">{{ book.total }} 册 ({{ book.available }} 可借)</n-descriptions-item>
            </n-descriptions>
            <n-divider />
            <h3>馆藏信息</h3>
            <HoldingsTable :items="book?.items || []" />
          </div>
        </div>
      </n-spin>
    </n-layout-content>
  </n-layout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { NLayout, NLayoutHeader, NLayoutContent, NButton, NSpace, NSpin, NDivider, NDescriptions, NDescriptionsItem, useMessage } from 'naive-ui'
import HoldingsTable from '../../components/HoldingsTable.vue'
import { api, bookApi } from '../../api'
import type { BookDetail } from '../../types/api'

const route = useRoute()
const message = useMessage()
const book = ref<BookDetail | null>(null)
const loading = ref(true)
const holdCount = ref(0)
function hasToken() { return !!localStorage.getItem('token') }

onMounted(async () => {
  const id = Number(route.params.id)
  try {
    book.value = await bookApi.getById(id)
    // Fetch hold count
    if (book.value && book.value.available === 0) {
      try {
        const res = await api.get<{ count: number }>('/holds/count?bookId=' + id)
        holdCount.value = res.count ?? 0
      } catch { holdCount.value = 0 }
    }
  } finally { loading.value = false }
})

function handleBorrow() {
  if (!hasToken()) { window.location.href = '/login'; return }
  // Redirect to reader books page
  window.location.href = '/reader/books'
}

async function handleHold() {
  if (!hasToken()) { window.location.href = '/login'; return }
  if (!book.value) return
  try {
    await api.post('/holds', { bookId: book.value.id })
    holdCount.value++
    message.success('预约成功！有书归还时将通知您。')
  } catch (e: unknown) { message.error((e as Error).message) }
}
</script>

<style scoped>
.detail-page { min-height: 100vh; background: var(--n-color-body); }
.header { padding: 0 24px; }
.header-inner { display: flex; align-items: center; justify-content: space-between; height: 56px; }
.content { display: flex; gap: 32px; padding: 24px; max-width: 1100px; margin: 0 auto; }
.cover-section { flex-shrink: 0; display: flex; flex-direction: column; align-items: center; }
.cover-img { width: 200px; height: 280px; object-fit: cover; border-radius: 6px; }
.cover-placeholder { width: 200px; height: 280px; display: flex; align-items: center; justify-content: center;
  background: linear-gradient(135deg, #5e6ad2, #7170ff); color: #fff; font-size: 60px; font-weight: bold; border-radius: 6px; }
.info-section { flex: 1; }
.info-section h2 { margin: 0 0 16px; font-size: 24px; }
.info-section h3 { margin: 0 0 12px; }
.desc-table { margin-bottom: 12px; }
</style>
