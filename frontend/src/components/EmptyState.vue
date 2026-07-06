<template>
  <div class="empty-state">
    <div class="empty-icon">
      <svg width="64" height="64" viewBox="0 0 64 64" fill="none">
        <rect x="8" y="12" width="48" height="44" rx="6" stroke="#c0c4d0" stroke-width="2" fill="none"/>
        <line x1="16" y1="24" x2="48" y2="24" stroke="#d0d4e0" stroke-width="2" stroke-linecap="round"/>
        <line x1="16" y1="32" x2="40" y2="32" stroke="#d0d4e0" stroke-width="2" stroke-linecap="round"/>
        <line x1="16" y1="40" x2="34" y2="40" stroke="#d0d4e0" stroke-width="2" stroke-linecap="round"/>
        <circle cx="48" cy="48" r="12" fill="#f0f2f5" stroke="#c0c4d0" stroke-width="1.5"/>
        <line x1="44" y1="48" x2="52" y2="48" stroke="#b0b4c0" stroke-width="2" stroke-linecap="round"/>
        <line x1="48" y1="44" x2="48" y2="52" stroke="#b0b4c0" stroke-width="2" stroke-linecap="round"/>
      </svg>
    </div>
    <div class="empty-message">{{ message || '暂无数据' }}</div>
  </div>
</template>

<script setup lang="ts">
defineProps<{ message?: string }>()
</script>

<style scoped>
.empty-state {
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  padding: 60px 0; gap: 16px;
}
.empty-icon { opacity: 0.6; }
.empty-message { font-size: 14px; color: var(--n-text-color-3); }
</style>
