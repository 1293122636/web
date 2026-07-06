<template>
  <div class="animated-bg">
    <div class="bg-gradient"></div>
    <div class="bg-particles">
      <div v-for="n in 20" :key="n" class="particle" :style="particleStyle(n)"></div>
    </div>
  </div>
</template>

<script setup lang="ts">
function particleStyle(n: number) {
  const size = 2 + Math.random() * 4
  return {
    width: `${size}px`,
    height: `${size}px`,
    left: `${Math.random() * 100}%`,
    animationDelay: `${Math.random() * 8}s`,
    animationDuration: `${6 + Math.random() * 8}s`,
    opacity: 0.15 + Math.random() * 0.25,
  }
}
</script>

<style scoped>
.animated-bg {
  position: fixed; inset: 0; overflow: hidden; z-index: 0;
  pointer-events: none;
}
.bg-gradient {
  position: absolute; inset: 0;
  background: linear-gradient(135deg, #0a0a12 0%, #13131e 50%, #0d0d18 100%);
}
.bg-particles { position: absolute; inset: 0; }
.particle {
  position: absolute; bottom: -10px;
  border-radius: 50%;
  background: rgba(94, 106, 210, 0.3);
  animation: floatUp linear infinite;
}
@keyframes floatUp {
  0% { transform: translateY(0) translateX(0); opacity: 0; }
  10% { opacity: 0.3; }
  90% { opacity: 0.1; }
  100% { transform: translateY(-100vh) translateX(30px); opacity: 0; }
}
</style>
