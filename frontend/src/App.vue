<template>
  <n-config-provider :theme-overrides="themeOverrides" :locale="zhCN" :date-locale="dateZhCN">
    <n-message-provider>
      <router-view />
    </n-message-provider>
  </n-config-provider>
</template>

<script setup lang="ts">
import { zhCN, dateZhCN, type GlobalThemeOverrides } from 'naive-ui'

const themeOverrides: GlobalThemeOverrides = {
  common: {
    primaryColor: '#5e6ad2',
    primaryColorHover: '#7170ff',
    primaryColorPressed: '#4f5bc4',
    primaryColorSuppl: '#7170ff',
    borderRadius: '8px',
    fontSize: '14px',
  },
  Card: { borderRadius: '10px', paddingMedium: '20px 24px' },
  DataTable: {
    borderRadius: '10px', thColor: '#f0f1f5', thFontWeight: '600',
    tdColorHover: 'rgba(94,106,210,0.04)',
  },
  Menu: { itemHeight: '44px', borderRadius: '8px' },
  Button: { borderRadius: '8px' },
  Input: { borderRadius: '8px' },
}
</script>
