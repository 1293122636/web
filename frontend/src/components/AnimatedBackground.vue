<template>
  <div class="animated-bg">
    <div class="gradient-layer" />
    <span v-for="i in 20" :key="i" class="particle" :style="particles[i-1]" />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const particles = computed(() =>
  Array.from({ length: 20 }, () => ({
    width: `${2 + Math.random() * 4}px`,
    height: `${2 + Math.random() * 4}px`,
    left: `${Math.random() * 100}%`,
    animationDelay: `${Math.random() * 15}s`,
    animationDuration: `${10 + Math.random() * 20}s`,
    '--opacity': `${0.15 + Math.random() * 0.2}`,
  }))
)
</script>

<style scoped>
.animated-bg {
  position: fixed; inset: 0; z-index: 0;
  background: #08090b; overflow: hidden;
}
.gradient-layer {
  position: absolute; inset: 0;
  background:
    radial-gradient(ellipse 80% 60% at 20% 30%, rgba(94,106,210,0.12) 0%, transparent 60%),
    radial-gradient(ellipse 60% 80% at 80% 70%, rgba(113,112,255,0.10) 0%, transparent 60%),
    radial-gradient(ellipse 50% 50% at 50% 50%, rgba(200,160,80,0.06) 0%, transparent 50%),
    radial-gradient(ellipse 40% 40% at 10% 80%, rgba(94,106,210,0.08) 0%, transparent 50%),
    radial-gradient(ellipse 30% 40% at 90% 20%, rgba(255,255,255,0.03) 0%, transparent 50%);
  animation: gradientShift 20s ease-in-out infinite alternate;
}
@keyframes gradientShift {
  0% { opacity: 0.8; transform: scale(1); }
  50% { opacity: 1; transform: scale(1.05); }
  100% { opacity: 0.85; transform: scale(1.02); }
}

.particle {
  position: absolute; bottom: -10px;
  background: rgba(113,112,255,0.5);
  border-radius: 50%;
  animation: floatUp linear infinite;
  pointer-events: none;
}
.particle:nth-child(even) { background: rgba(200,160,80,0.35); }
.particle:nth-child(4n) { background: rgba(255,255,255,0.25); }

@keyframes floatUp {
  0%   { transform: translateY(0) translateX(0) scale(1); opacity: var(--opacity, 0.2); }
  60%  { opacity: calc(var(--opacity, 0.2) * 0.5); }
  100% { transform: translateY(-100vh) translateX(30px) scale(0.3); opacity: 0; }
}
</style>
