<template>
  <n-layout has-sider style="display: flex; min-height: 100vh;">
    <n-layout-sider bordered class="admin-sider" collapse-mode="width" :collapsed-width="64" :width="240" :native-scrollbar="false">
      <div class="logo">
        <svg class="logo-icon" width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
          <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/>
          <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/>
          <line x1="8" y1="7" x2="16" y2="7"/>
          <line x1="8" y1="11" x2="14" y2="11"/>
        </svg>
        <div>
          <n-text tag="div" style="font-size: 16px; font-weight: 600; color: #e8e8f0; letter-spacing: 0.3px;">图书馆管理</n-text>
          <n-text tag="div" style="font-size: 11px; color: #7a7a9a; letter-spacing: 0.5px; text-transform: uppercase;">ADMIN</n-text>
        </div>
      </div>
      <div class="sider-divider"></div>
      <n-menu
        inverted
        :value="activeKey"
        :options="menuOptions"
        :root-indent="16"
        :indent="12"
        @update:value="handleMenu"
      />
      <div class="footer">
        <n-button text @click="handleLogout" style="width: 100%; color: #8888a0;">
          <template #icon><n-icon><LogOutOutline /></n-icon></template>
          退出登录
        </n-button>
      </div>
    </n-layout-sider>
    <n-layout-content class="content">
      <router-view />
    </n-layout-content>
  </n-layout>
</template>

<script setup lang="ts">
import { computed, h } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { NIcon } from 'naive-ui'
import {
  GridOutline, BookOutline, PricetagsOutline, PeopleOutline,
  SwapHorizontalOutline, CashOutline, BarChartOutline, SettingsOutline,
  CameraOutline, LogOutOutline
} from '@vicons/ionicons5'
import { useAuthStore } from '@/stores/auth'
import type { MenuOption } from 'naive-ui'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()

const renderIcon = (icon: any) => () => h(NIcon, null, () => h(icon))

const menuOptions: MenuOption[] = [
  { label: '概览', key: '/admin/dashboard', icon: renderIcon(GridOutline) },
  { label: '图书管理', key: '/admin/books', icon: renderIcon(BookOutline) },
  { label: '分类管理', key: '/admin/categories', icon: renderIcon(PricetagsOutline) },
  { label: '读者管理', key: '/admin/readers', icon: renderIcon(PeopleOutline) },
  { label: '借阅管理', key: '/admin/borrows', icon: renderIcon(SwapHorizontalOutline) },
  { label: '罚款管理', key: '/admin/fines', icon: renderIcon(CashOutline) },
  { label: '统计报表', key: '/admin/stats', icon: renderIcon(BarChartOutline) },
  { label: '流通台', key: '/admin/circulation', icon: renderIcon(CameraOutline) },
  { label: '系统设置', key: '/admin/settings', icon: renderIcon(SettingsOutline) },
]

const activeKey = computed(() => {
  const p = route.path
  return menuOptions.find(m => p.startsWith(m.key as string))?.key ?? null
})

function handleMenu(key: string) { router.push(key) }
function handleLogout() { auth.logout(); router.push('/login') }
</script>

<style scoped>
.admin-sider {
  background: #16161e !important;
  border-right: 1px solid rgba(255,255,255,0.06) !important;
}
.logo { display: flex; align-items: center; gap: 12px; padding: 20px 20px 16px; }
.logo-icon { width: 26px; height: 26px; color: #7a7a9a; flex-shrink: 0; }
.sider-divider {
  height: 1px; background: rgba(255,255,255,0.06); margin: 0 16px 8px;
}
.content {
  padding: 28px 32px;
  min-height: 100vh;
  background: var(--n-color-body);
}
.footer {
  position: absolute; bottom: 0; width: 100%;
  padding: 16px 20px; border-top: 1px solid rgba(255,255,255,0.06);
}
</style>
