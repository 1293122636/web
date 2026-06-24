<template>
  <div style="display: flex; align-items: center; justify-content: center; min-height: 100vh;">
    <n-card title="图书馆管理系统" style="width: 400px;" size="large">
      <n-form ref="formRef" :model="form" :rules="rules">
        <n-form-item path="username" label="用户名">
          <n-input v-model:value="form.username" placeholder="请输入用户名" size="large" />
        </n-form-item>
        <n-form-item path="password" label="密码">
          <n-input v-model:value="form.password" type="password" placeholder="请输入密码" size="large" />
        </n-form-item>
      </n-form>

      <n-button type="primary" block size="large" :loading="loading" @click="handleLogin" style="margin-top: 8px;">
        登录
      </n-button>

      <n-text depth="3" style="display: block; text-align: center; margin-top: 16px; font-size: 13px; cursor: pointer;" @click="showRegister = true">
        没有账号？<n-text type="primary">注册</n-text>
      </n-text>
    </n-card>

    <!-- Register Modal -->
    <n-modal v-model:show="showRegister" preset="card" title="读者注册" style="width: 420px;">
      <n-form ref="regFormRef" :model="reg" :rules="regRules">
        <n-form-item path="username" label="用户名">
          <n-input v-model:value="reg.username" placeholder="用户名" />
        </n-form-item>
        <n-form-item path="password" label="密码">
          <n-input v-model:value="reg.password" type="password" placeholder="密码" />
        </n-form-item>
        <n-form-item path="name" label="真实姓名">
          <n-input v-model:value="reg.name" placeholder="真实姓名" />
        </n-form-item>
        <n-form-item path="phone" label="手机号">
          <n-input v-model:value="reg.phone" placeholder="手机号（可选）" />
        </n-form-item>
      </n-form>
      <template #footer>
        <div style="display: flex; gap: 8px; justify-content: flex-end;">
          <n-button @click="showRegister = false">取消</n-button>
          <n-button type="primary" :loading="regLoading" @click="handleRegister">注册</n-button>
        </div>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { api, setAuth } from '../api'

const router = useRouter()
const message = useMessage()

const form = ref({ username: '', password: '' })
const loading = ref(false)
const rules = {
  username: [{ required: true, message: '请输入用户名' }],
  password: [{ required: true, message: '请输入密码' }]
}

async function handleLogin() {
  if (!form.value.username || !form.value.password) { message.warning('请输入用户名和密码'); return }
  loading.value = true
  try {
    const res = await api.login(form.value.username, form.value.password)
    setAuth(res.user, res.token)
    message.success('登录成功')
    router.push(res.user.role === 'admin' ? '/admin/dashboard' : '/reader/books')
  } catch (e: unknown) {
    message.error((e as Error).message || '登录失败')
  } finally {
    loading.value = false
  }
}

const showRegister = ref(false)
const regLoading = ref(false)
const reg = ref({ username: '', password: '', name: '', phone: '' })
const regRules = {
  username: [{ required: true, message: '请输入用户名' }],
  password: [{ required: true, min: 6, message: '密码至少6位' }],
  name: [{ required: true, message: '请输入姓名' }]
}

async function handleRegister() {
  if (!reg.value.username || !reg.value.password || !reg.value.name) {
    message.warning('请填写必填项'); return
  }
  regLoading.value = true
  try {
    const res = await api.register(reg.value)
    setAuth(res.user, res.token)
    showRegister.value = false
    message.success('注册成功')
    router.push('/reader/books')
  } catch (e: unknown) {
    message.error((e as Error).message || '注册失败')
  } finally {
    regLoading.value = false
  }
}
</script>
