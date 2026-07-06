<template>
  <n-layout has-sider style="display: flex; min-height: 100vh;">
    <n-layout-sider bordered collapse-mode="width" :collapsed-width="64" :width="240" :native-scrollbar="false" class="reader-sider">
      <div class="logo">
          <svg class="logo-icon" width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/>
            <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/>
            <line x1="8" y1="7" x2="16" y2="7"/>
            <line x1="8" y1="11" x2="14" y2="11"/>
          </svg>
        <div>
          <n-text tag="div" style="font-size: 16px; font-weight: 600;">图书馆</n-text>
          <n-text tag="div" style="font-size: 12px; color: #8888a0;">读者</n-text>
        </div>
      </div>

      <n-menu
        inverted
        :value="activeKey"
        :options="menuOptions"
        :root-indent="20"
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
import { BookOutline, ClipboardOutline, PersonOutline, LogOutOutline } from '@vicons/ionicons5'
import { useAuthStore } from '../../stores/auth'
import type { MenuOption } from 'naive-ui'

const router = useRouter()
const route = useRoute()

const renderIcon = (icon: any) => () => h(NIcon, null, () => h(icon))

const menuOptions: MenuOption[] = [
  { label: '图书浏览', key: '/reader/books', icon: renderIcon(BookOutline) },
  { label: '我的借阅', key: '/reader/my-borrows', icon: renderIcon(ClipboardOutline) },
  { label: '个人信息', key: '/reader/profile', icon: renderIcon(PersonOutline) },
]

const activeKey = computed(() => {
  const p = route.path
  return menuOptions.find(m => p.startsWith(m.key as string))?.key ?? null
})

function handleMenu(key: string) { router.push(key) }
function handleLogout() { useAuthStore().logout(); router.push('/login') }
</script>

<style scoped>
.reader-sider {
  background: #16161e !important;
  border-right: 1px solid rgba(255,255,255,0.06) !important;
}
.logo { display: flex; align-items: center; gap: 12px; padding: 18px 20px 14px; }
.logo-icon { width: 26px; height: 26px; color: #7a7a9a; flex-shrink: 0; }
.content { padding: 28px 32px; min-height: 100vh; background: var(--n-color-body); }
.footer { position: absolute; bottom: 0; width: 100%; padding: 16px 20px; border-top: 1px solid rgba(255,255,255,0.06); }
</style>
