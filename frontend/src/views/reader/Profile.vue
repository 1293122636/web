<template>
  <div>
    <n-h1 prefix="bar" style="margin-bottom: 20px;"><n-text type="primary">个人信息</n-text></n-h1>
    <n-card style="max-width: 480px;">
      <n-spin :show="loading">
        <n-form :model="form" label-placement="top">
          <n-form-item label="用户名"><n-text>{{ form.username }}</n-text></n-form-item>
          <n-form-item label="角色"><n-tag :type="form.role === 'admin' ? 'error' : 'info'" size="small">{{ form.role === 'admin' ? '管理员' : '读者' }}</n-tag></n-form-item>
          <n-form-item label="姓名">
            <n-input v-model:value="form.name" placeholder="真实姓名" />
          </n-form-item>
          <n-form-item label="手机号">
            <n-input v-model:value="form.phone" placeholder="手机号" />
          </n-form-item>
          <n-form-item label="邮箱">
            <n-input v-model:value="form.email" placeholder="邮箱" />
          </n-form-item>
          <n-form-item label="注册时间">
            <n-text depth="3">{{ form.createdAt ? new Date(form.createdAt).toLocaleString('zh-CN') : '-' }}</n-text>
          </n-form-item>
        </n-form>
        <n-button type="primary" :loading="saving" @click="handleSave" style="margin-top: 16px;">保存修改</n-button>
      </n-spin>
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useMessage } from 'naive-ui'
import api from '@/api'
import type { UserProfile } from '@/types/api'

const message = useMessage()
const loading = ref(false)
const saving = ref(false)
const form = reactive({ username: '', role: '', name: '', phone: '', email: '', createdAt: '' })

onMounted(async () => {
  loading.value = true
  try {
    const { data } = await api.get<UserProfile>('/auth/me')
    Object.assign(form, data)
  } catch { message.error('获取信息失败') }
  loading.value = false
})

async function handleSave() {
  saving.value = true
  try {
    await api.put('/readers/profile', { name: form.name, phone: form.phone, email: form.email })
    message.success('已保存')
  } catch (e: unknown) { message.error((e as Error).message) }
  saving.value = false
}
</script>
