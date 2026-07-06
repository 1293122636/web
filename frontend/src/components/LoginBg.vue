<template>
  <div class="login-bg-wrap">
    <div
      class="login-bg-image"
      :style="{ backgroundImage: `url(${bgImg})` }"
    ></div>
    <div class="login-bg-clouds"></div>
    <div class="login-bg-overlay"></div>
  </div>
</template>

<script setup lang="ts">
const bgImg = '/library-bg.jpg'
</script>

<style scoped>
.login-bg-wrap {
  position: fixed; inset: 0; overflow: hidden;
}
.login-bg-image {
  position: absolute; inset: 0;
  background-size: cover; background-position: center;
  background-repeat: no-repeat;
}
.login-bg-clouds {
  position: absolute; inset: 0;
  overflow: hidden;
  pointer-events: none;
}
.login-bg-clouds::before {
  content: '';
  position: absolute;
  top: 0; left: -20%; width: 140%; height: 40%;
  background:
    radial-gradient(ellipse 55% 35% at 18% 25%, rgba(255,220,180,0.06) 0%, transparent 55%),
    radial-gradient(ellipse 50% 30% at 38% 20%, rgba(255,220,180,0.05) 0%, transparent 50%),
    radial-gradient(ellipse 60% 35% at 58% 28%, rgba(255,210,170,0.04) 0%, transparent 55%),
    radial-gradient(ellipse 45% 30% at 78% 22%, rgba(255,220,180,0.05) 0%, transparent 50%);
  animation: cloudDrift 45s linear infinite;
}
.login-bg-overlay {
  position: absolute; inset: 0;
  background: radial-gradient(ellipse at 50% 40%, rgba(0,0,0,0.08) 0%, rgba(0,0,0,0.25) 50%, rgba(0,0,0,0.40) 100%);
}
</style>

<style>
@keyframes cloudDrift {
  0%   { transform: translateX(0); }
  100% { transform: translateX(28%); }
}
</style>
