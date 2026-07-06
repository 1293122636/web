<template>
  <div>
    <div class="page-header">
      <h1 class="page-title">管理概览</h1>
      <n-tag type="primary" size="small" round>v0.3.3</n-tag>
    </div>

    <n-grid :cols="5" :x-gap="16" :y-gap="16" responsive="screen" style="margin-top: 8px">
      <n-grid-item v-for="(s, i) in stats" :key="s.label">
        <div class="stat-card" :style="{ '--accent': colors[i] }">
          <div class="stat-icon-wrap">
            <n-icon :size="22" :color="colors[i]"><component :is="icons[i]" /></n-icon>
          </div>
          <div class="stat-body">
            <span class="stat-value">{{ s.value }}</span>
            <span class="stat-label">{{ s.label }}</span>
          </div>
          <div class="stat-glow" :style="{ background: `radial-gradient(ellipse, ${colors[i]}22 0%, transparent 70%)` }"></div>
        </div>
      </n-grid-item>
    </n-grid>

    <n-grid :cols="2" :x-gap="20" style="margin-top: 24px">
      <n-grid-item>
        <n-card class="panel-card" size="small">
          <template #header>
            <div class="panel-header"><n-icon size="18"><FlashOutline /></n-icon> 快捷操作</div>
          </template>
          <div class="quick-actions">
            <div class="action-item" @click="$router.push('/admin/books')">
              <div class="action-icon" style="background: #5e6ad222; color: #5e6ad2"><n-icon size="22"><BookOutline /></n-icon></div>
              <div class="action-text"><span class="action-name">图书管理</span><span class="action-desc">增删改查图书信息</span></div>
            </div>
            <div class="action-item" @click="$router.push('/admin/borrows')">
              <div class="action-icon" style="background: #f0a02022; color: #f0a020"><n-icon size="22"><SwapHorizontalOutline /></n-icon></div>
              <div class="action-text"><span class="action-name">借阅管理</span><span class="action-desc">处理借书、还书、续借</span></div>
            </div>
            <div class="action-item" @click="$router.push('/admin/readers')">
              <div class="action-icon" style="background: #18a05822; color: #18a058"><n-icon size="22"><PeopleOutline /></n-icon></div>
              <div class="action-text"><span class="action-name">读者管理</span><span class="action-desc">管理读者信息</span></div>
            </div>
            <div class="action-item" @click="$router.push('/admin/circulation')">
              <div class="action-icon" style="background: #2080f022; color: #2080f0"><n-icon size="22"><ScanOutline /></n-icon></div>
              <div class="action-text"><span class="action-name">流通台</span><span class="action-desc">扫码借还操作</span></div>
            </div>
            <div class="action-item" @click="$router.push('/admin/stats')">
              <div class="action-icon" style="background: #d0305022; color: #d03050"><n-icon size="22"><BarChartOutline /></n-icon></div>
              <div class="action-text"><span class="action-name">统计报表</span><span class="action-desc">借阅量、热门图书</span></div>
            </div>
          </div>
        </n-card>
      </n-grid-item>
      <n-grid-item>
        <n-card class="panel-card" size="small">
          <template #header>
            <div class="panel-header"><n-icon size="18"><InformationCircleOutline /></n-icon> 系统信息</div>
          </template>
          <n-descriptions :column="1" size="small" label-placement="left">
            <n-descriptions-item label="API 端点">
              <n-tag size="small" type="info">40</n-tag>
            </n-descriptions-item>
            <n-descriptions-item label="测试覆盖">
              <n-tag size="small" type="success">51/51 通过</n-tag>
            </n-descriptions-item>
            <n-descriptions-item label="构建状态">
              <n-tag size="small" type="success">✅ PASS</n-tag>
            </n-descriptions-item>
            <n-descriptions-item label="框架版本">
              <n-space size="small">
                <n-tag size="tiny">Spring Boot 3.2</n-tag>
                <n-tag size="tiny">MyBatis 3.0</n-tag>
                <n-tag size="tiny">Vue 3.5</n-tag>
                <n-tag size="tiny">Naive UI 2.44</n-tag>
              </n-space>
            </n-descriptions-item>
          </n-descriptions>
        </n-card>
      </n-grid-item>
    </n-grid>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { NIcon, NTag, NSpace } from 'naive-ui'
import {
  BookOutline, PeopleOutline, LibraryOutline, GridOutline, AlertCircleOutline,
  SwapHorizontalOutline, ScanOutline, BarChartOutline, FlashOutline,
  InformationCircleOutline
} from '@vicons/ionicons5'
import api from '@/api'

const colors = ['#5e6ad2', '#f0a020', '#18a058', '#2080f0', '#d03050']
const icons = [LibraryOutline, SwapHorizontalOutline, PeopleOutline, GridOutline, AlertCircleOutline]

const stats = ref([
  { label: '总藏书', value: '-' },
  { label: '在借数量', value: '-' },
  { label: '读者总数', value: '-' },
  { label: '分类数', value: '-' },
  { label: '逾期未还', value: '-' }
])

onMounted(async () => {
  try {
    const { data } = await api.get('/stats')
    stats.value = [
      { label: '总藏书', value: data.totalBooks || 0 },
      { label: '在借数量', value: data.activeBorrows || 0 },
      { label: '读者总数', value: data.totalReaders || 0 },
      { label: '分类数', value: data.totalCategories || 0 },
      { label: '逾期未还', value: data.overdueCount || 0 }
    ]
  } catch (e) { console.error('fetchStats failed:', e) }
})
</script>

<style scoped>
.page-header {
  display: flex; align-items: center; gap: 12px; margin-bottom: 4px;
}
.page-title {
  font-size: 22px; font-weight: 700; color: var(--n-text-color); margin: 0;
}
.stat-card {
  position: relative; overflow: hidden;
  background: var(--n-card-color); border-radius: 12px;
  padding: 20px; display: flex; align-items: center; gap: 16px;
  border: 1px solid var(--n-border-color);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}
.stat-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(0,0,0,0.08);
}
.stat-icon-wrap {
  width: 46px; height: 46px; border-radius: 12px;
  display: flex; align-items: center; justify-content: center;
  background: color-mix(in srgb, var(--accent) 12%, transparent);
  flex-shrink: 0;
}
.stat-body { display: flex; flex-direction: column; gap: 2px; }
.stat-value { font-size: 26px; font-weight: 700; color: var(--n-text-color); line-height: 1.1; }
.stat-label { font-size: 12px; color: var(--n-text-color-3); }
.stat-glow {
  position: absolute; top: -20px; right: -20px; width: 100px; height: 100px;
  pointer-events: none; border-radius: 50%;
}
.panel-card { height: 100%; }
.panel-header { display: flex; align-items: center; gap: 6px; font-size: 15px; font-weight: 600; }
.quick-actions { display: flex; flex-direction: column; gap: 4px; }
.action-item {
  display: flex; align-items: center; gap: 14px; padding: 10px 12px;
  border-radius: 10px; cursor: pointer;
  transition: background 0.15s ease, transform 0.15s ease;
}
.action-item:hover { background: var(--n-color); transform: translateX(4px); }
.action-icon {
  width: 42px; height: 42px; border-radius: 10px;
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}
.action-text { display: flex; flex-direction: column; gap: 1px; }
.action-name { font-size: 14px; font-weight: 600; color: var(--n-text-color); }
.action-desc { font-size: 12px; color: var(--n-text-color-3); }
</style>
