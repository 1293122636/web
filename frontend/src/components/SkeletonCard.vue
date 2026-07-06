<template>
  <div class="skeleton-card">
    <div class="sk-cover"></div>
    <div class="sk-body">
      <div class="sk-line sk-title"></div>
      <div class="sk-line sk-author"></div>
      <div class="sk-line sk-meta"></div>
    </div>
  </div>
</template>

<style scoped>
.skeleton-card {
  width: 190px; border-radius: 12px; overflow: hidden;
  background: var(--n-card-color); border: 1px solid var(--n-border-color);
  padding: 16px 17px 18px;
}
.sk-cover {
  width: 156px; height: 200px; margin: 0 auto 12px; border-radius: 8px;
  background: linear-gradient(90deg, #e8e8ee 25%, #f0f0f5 50%, #e8e8ee 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s ease-in-out infinite;
}
.sk-body { display: flex; flex-direction: column; gap: 8px; align-items: center; }
.sk-line { height: 12px; border-radius: 6px; background: linear-gradient(90deg, #e8e8ee 25%, #f0f0f5 50%, #e8e8ee 75%); background-size: 200% 100%; animation: shimmer 1.5s ease-in-out infinite; }
.sk-title { width: 80%; }
.sk-author { width: 55%; height: 10px; }
.sk-meta { width: 40%; height: 10px; }
@keyframes shimmer { 0% { background-position: 200% 0; } 100% { background-position: -200% 0; } }
</style>
