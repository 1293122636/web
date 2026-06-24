import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { api, setAuth, clearAuth, getUser } from '../api'
import type { UserProfile } from '../types/api'

export const useAuthStore = defineStore('auth', () => {
  const user = ref<UserProfile | null>(getUser())
  const token = ref<string | null>(localStorage.getItem('token'))
  const isAdmin = computed(() => user.value?.role === 'admin')
  const isLoggedIn = computed(() => !!token.value)

  async function login(username: string, password: string) {
    const res = await api.login(username, password)
    setAuth(res.user, res.token)
    user.value = res.user
    token.value = res.token
    return res
  }

  async function register(data: { username: string; password: string; name: string }) {
    const res = await api.register(data)
    setAuth(res.user, res.token)
    user.value = res.user
    token.value = res.token
    return res
  }

  function logout() {
    clearAuth()
    user.value = null
    token.value = null
  }

  return { user, token, isAdmin, isLoggedIn, login, register, logout }
})
