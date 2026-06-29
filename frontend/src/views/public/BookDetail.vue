<template>
  <n-layout class="detail-page">
    <n-layout-header bordered class="header">
      <div class="header-inner">
        <n-button text @click="$router.push('/books')">
          <template #icon><n-icon><ArrowBackOutline /></n-icon></template>返回搜索
        </n-button>
        <n-button text @click="$router.push('/login')">
          <template #icon><n-icon><PersonOutline /></n-icon></template>登录
        </n-button>
      </div>
    </n-layout-header>
    <n-layout-content>
      <n-spin :show="loading">
        <div v-if="book" class="content">
          <div class="cover-section">
            <img v-if="book.cover" :src="book.cover" class="cover-img" />
            <div v-else class="cover-placeholder">
              <span class="cover-letter">{{ book.title[0] }}</span>
            </div>
            <div class="cover-actions">
              <n-button type="primary" size="large" block @click="handleBorrow">
                <template #icon><n-icon><BookOutline /></n-icon></template>
                {{ book.available > 0 ? '借阅此书' : '预约此书' }}
              </n-button>
              <n-button v-if="book.available === 0" type="warning" block style="margin-top:8px" @click="handleHold" :loading="holdLoading">
                <template #icon><n-icon><TimeOutline /></n-icon></template>
                预约排队 ({{ holdCount }} 人)
              </n-button>
              <n-tag v-else type="success" size="large" style="margin-top:8px;width:100%;text-align:center;justify-content:center">
                馆藏 {{ book.total }} 册 · {{ book.available }} 册可借
              </n-tag>
            </div>
          </div>
          <div class="info-section">
            <h2 class="book-title">{{ book.title }}</h2>
            <p class="book-author">{{ book.author }} · {{ book.publisher || '未知出版社' }} · {{ book.year || '-' }}</p>
            <n-divider />
            <n-descriptions :column="2" bordered size="small" label-placement="left">
              <n-descriptions-item label="ISBN">{{ book.isbn }}</n-descriptions-item>
              <n-descriptions-item label="分类号">{{ book.clcNumber || '-' }}</n-descriptions-item>
              <n-descriptions-item label="语种">{{ book.language || '-' }}</n-descriptions-item>
              <n-descriptions-item label="国别">{{ book.country || '-' }}</n-descriptions-item>
              <n-descriptions-item label="载体形态">{{ book.physicalDesc || '-' }}</n-descriptions-item>
              <n-descriptions-item label="分类">{{ book.category?.name || '-' }}</n-descriptions-item>
            </n-descriptions>
            <n-divider />
            <h3 class="section-title">馆藏信息</h3>
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
import { NIcon } from 'naive-ui'
import { useMessage } from 'naive-ui'
import { ArrowBackOutline, PersonOutline, BookOutline, TimeOutline } from '@vicons/ionicons5'
import HoldingsTable from '../../components/HoldingsTable.vue'
import api, { bookApi } from '../../api'
import type { BookDetail } from '../../types/api'

const route = useRoute()
const message = useMessage()
const book = ref<BookDetail | null>(null)
const loading = ref(true)
const holdCount = ref(0)
const holdLoading = ref(false)
function hasToken() { return !!localStorage.getItem('token') }

onMounted(async () => {
  const id = Number(route.params.id)
  loading.value = true
  try {
    book.value = (await bookApi.getById(id)).data
    if (book.value && book.value.available === 0) {
      try {
        const res = await api.get('/holds/count', { params: { bookId: id } })
        holdCount.value = res.data?.count ?? 0
      } catch (e) { console.error('fetchHoldCount failed:', e); holdCount.value = 0 }
    }
  } finally { loading.value = false }
})

function handleBorrow() {
  if (!hasToken()) { window.location.href = '/login'; return }
  window.location.href = '/reader/books'
}
async function handleHold() {
  if (!hasToken()) { window.location.href = '/login'; return }
  if (!book.value) return
  holdLoading.value = true
  try {
    await api.post('/holds', { bookId: book.value.id })
    holdCount.value++
    message.success('预约成功！有书归还时将通知您。')
  } catch (e: unknown) { message.error((e as Error).message) }
  holdLoading.value = false
}
</script>

<style scoped>
.detail-page { min-height: 100vh; background: var(--n-color-body); }
.header {
  padding: 0 28px; height: 56px; display: flex; align-items: center;
  background: linear-gradient(135deg, #5e6ad2 0%, #7c6fdb 100%);
}
.header-inner { display: flex; justify-content: space-between; width: 100%; }
.header :deep(.n-button) { color: rgba(255,255,255,0.85) !important; }
.header :deep(.n-button:hover) { color: #fff !important; }
.content { display: flex; gap: 40px; padding: 32px 28px; max-width: 1100px; margin: 0 auto; }
.cover-section { flex-shrink: 0; width: 240px; }
.cover-img { width: 240px; height: 340px; object-fit: cover; border-radius: 8px; box-shadow: 0 4px 20px rgba(0,0,0,0.3); }
.cover-placeholder { width: 240px; height: 340px; display: flex; align-items: center; justify-content: center;
  background: linear-gradient(135deg, #5e6ad2 0%, #7170ff 50%, #a78bfa 100%);
  border-radius: 8px; box-shadow: 0 4px 20px rgba(94,106,210,0.3); }
.cover-letter { font-size: 80px; font-weight: 700; color: rgba(255,255,255,0.9); }
.cover-actions { margin-top: 20px; }
.info-section { flex: 1; min-width: 0; }
.book-title { margin: 0 0 4px; font-size: 26px; font-weight: 700; }
.book-author { margin: 0 0 8px; font-size: 14px; color: var(--n-text-color-3); }
.section-title { margin: 0 0 12px; font-size: 16px; font-weight: 600; }
</style>
