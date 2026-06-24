<template>
  <n-layout style="display: flex; min-height: 100vh;">
    <n-layout-sider bordered collapse-mode="width" :collapsed-width="64" :width="240" :native-scrollbar="false">
      <div style="padding: 18px 20px 12px;">
        <n-text tag="div" depth="1" style="font-size: 16px; font-weight: 510;">图书馆</n-text>
        <n-text tag="div" depth="3" style="font-size: 12px; margin-top: 2px;">读者</n-text>
      </div>

      <n-menu
        :value="activeKey"
        :options="menuOptions"
        :root-indent="20"
        :indent="12"
        @update:value="handleMenu"
      />

      <div style="position: absolute; bottom: 0; width: 100%; padding: 20px; border-top: 1px solid var(--n-border-color);">
        <n-button text @click="handleLogout" style="width: 100%; color: var(--n-text-color-3);">
          退出登录
        </n-button>
      </div>
    </n-layout-sider>

    <n-layout-content style="padding: 28px 32px; min-height: 100vh;">
      <router-view />
    </n-layout-content>
  </n-layout>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { clearAuth } from '../../api'
import type { MenuOption } from 'naive-ui'

const router = useRouter()
const route = useRoute()

const menuOptions: MenuOption[] = [
  { label: '图书浏览', key: '/reader/books', icon: () => '📚' },
  { label: '我的借阅', key: '/reader/my-borrows', icon: () => '📋' },
  { label: '个人信息', key: '/reader/profile', icon: () => '👤' }
]

const activeKey = computed(() => {
  const p = route.path
  return menuOptions.find(m => p.startsWith(m.key as string))?.key ?? null
})

function handleMenu(key: string) {
  router.push(key)
}

function handleLogout() {
  clearAuth()
  router.push('/login')
}
</script>
