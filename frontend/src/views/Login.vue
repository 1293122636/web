<template>
  <div class="login-page">
    <LoginBg />
    <n-config-provider :theme="darkTheme">
      <div class="login-wrapper">
      <div class="login-card">
        <div class="back-bar">
          <div class="back-btn" @click="$router.push('/books')">
            <span class="back-arrow">←</span>
            <span class="back-text">浏览书目</span>
          </div>
        </div>
        <div class="logo-area">
          <img class="school-badge" src="https://www.sdust.edu.cn/web202504/images/xqlogo3.png" width="72" height="72" alt="山东科技大学" />
          <div class="brand-text">
            <h1 class="brand-title">图书馆管理系统</h1>
            <p class="brand-subtitle">SHANDONG UNIVERSITY OF SCIENCE AND TECHNOLOGY</p>
          </div>
        </div>
        <n-form ref="formRef" :model="form" :rules="rules" size="large">
          <n-form-item path="username">
            <n-input v-model:value="form.username" placeholder="用户名" clearable>
              <template #prefix><n-icon><PersonOutline /></n-icon></template>
            </n-input>
          </n-form-item>
          <n-form-item path="password">
            <n-input v-model:value="form.password" type="password" placeholder="密码" show-password-on="click">
              <template #prefix><n-icon><LockClosedOutline /></n-icon></template>
            </n-input>
          </n-form-item>
        </n-form>
        <n-button type="primary" block size="large" :loading="loading" @click="handleLogin" style="margin-top: 4px;">登录</n-button>
        <n-text depth="3" class="register-link" @click="showRegister = true">没有账号？<n-text type="primary" style="cursor:pointer">立即注册</n-text></n-text>
      </div>
      <n-modal v-model:show="showRegister" preset="card" title="读者注册" style="width:420px">
        <n-form ref="regFormRef" :model="reg" :rules="regRules" label-placement="top">
          <n-form-item path="username" label="用户名"><n-input v-model:value="reg.username" placeholder="用户名" /></n-form-item>
          <n-form-item path="password" label="密码"><n-input v-model:value="reg.password" type="password" placeholder="密码（至少6位）" /></n-form-item>
          <n-form-item path="name" label="真实姓名"><n-input v-model:value="reg.name" placeholder="真实姓名" /></n-form-item>
          <n-form-item path="phone" label="手机号"><n-input v-model:value="reg.phone" placeholder="手机号（可选）" /></n-form-item>
        </n-form>
        <template #footer><n-space justify="end"><n-button @click="showRegister=false">取消</n-button><n-button type="primary" :loading="regLoading" @click="handleRegister">注册</n-button></n-space></template>
      </n-modal>
    </div>
  </n-config-provider>
</div></template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage, NIcon, darkTheme } from 'naive-ui'
import { PersonOutline, LockClosedOutline } from '@vicons/ionicons5'
import api from '../api'
import LoginBg from '../components/LoginBg.vue'
import { useAuthStore } from '../stores/auth'
import type { LoginResponse, UserProfile } from '../types/api'
const router = useRouter(); const message = useMessage()
const form = ref({ username: '', password: '' }); const loading = ref(false)
const rules = { username: [{ required: true, message: '请输入用户名' }], password: [{ required: true, message: '请输入密码' }] }
async function handleLogin() {
  if (!form.value.username || !form.value.password) { message.warning('请输入用户名和密码'); return }
  loading.value = true
  try { const { data } = await api.post<LoginResponse>('/auth/login', form.value); useAuthStore().login(data); message.success('登录成功'); router.push(data.user.role === 'admin' ? '/admin/dashboard' : '/reader/books') }
  catch (e: unknown) { message.error((e as Error).message || '登录失败') } finally { loading.value = false }
}
const showRegister = ref(false); const regLoading = ref(false)
const reg = ref({ username: '', password: '', name: '', phone: '' })
const regRules = { username: [{ required: true, message: '请输入用户名' }], password: [{ required: true, min: 6, message: '密码至少6位' }], name: [{ required: true, message: '请输入姓名' }] }
async function handleRegister() {
  if (!reg.value.username || !reg.value.password || !reg.value.name) { message.warning('请填写必填项'); return }
  regLoading.value = true
  try { const { data } = await api.post<LoginResponse>('/auth/register', reg.value); useAuthStore().login(data); showRegister.value = false; message.success('注册成功'); router.push('/reader/books') }
  catch (e: unknown) { message.error((e as Error).message || '注册失败') } finally { regLoading.value = false }
}
</script>

<style scoped>
.login-wrapper { display: flex; align-items: center; justify-content: center; min-height: 100vh; }
.login-card {
  width: 420px; padding: 44px 40px 36px;
  background: rgba(15,16,22,0.75);
  border: 1px solid rgba(255,255,255,0.06);
  border-radius: 20px;
  backdrop-filter: blur(28px);
  box-shadow: 0 12px 50px rgba(0,0,0,0.5);
  position: relative; z-index: 1;
}
.logo-area {
  display: flex; align-items: center; gap: 18px;
  margin-bottom: 32px; text-align: left;
}
.school-badge { flex-shrink: 0; filter: drop-shadow(0 2px 8px rgba(0,0,0,0.3)); }
.brand-text { display: flex; flex-direction: column; gap: 4px; }
.brand-title {
  margin: 0; font-size: 24px; font-weight: 700;
  background: linear-gradient(135deg, #e8e8f0 0%, #c8c8e0 50%, #a8a8d0 100%);
  -webkit-background-clip: text; -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: 1.5px;
}
.brand-subtitle {
  margin: 0; font-size: 11px;
  color: rgba(255,255,255,0.35);
  letter-spacing: 2.5px;
  font-weight: 300;
}
.register-link { display: block; text-align: center; margin-top: 18px; font-size: 13px; cursor: pointer; }
.back-bar {
  display: flex; justify-content: flex-start; margin-bottom: 4px;
}
.back-btn {
  display: inline-flex; align-items: center; gap: 8px;
  padding: 7px 16px 7px 12px; border-radius: 100px;
  cursor: pointer; user-select: none;
  background: rgba(255,255,255,0.06);
  border: 1px solid rgba(255,255,255,0.08);
  transition: all 0.25s ease;
  backdrop-filter: blur(12px);
}
.back-btn:hover {
  background: rgba(255,255,255,0.12);
  border-color: rgba(255,255,255,0.15);
  transform: translateX(-3px);
  box-shadow: 0 4px 15px rgba(0,0,0,0.2);
}
.back-arrow {
  display: inline-block; font-size: 18px; line-height: 1;
  color: rgba(255,255,255,0.6);
  transition: transform 0.25s ease;
}
.back-btn:hover .back-arrow { transform: translateX(-4px); }
.back-text {
  font-size: 13px; font-weight: 500;
  color: rgba(255,255,255,0.7);
  letter-spacing: 0.5px;
}
</style>
