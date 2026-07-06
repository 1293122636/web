import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getUser, setAuth, clearAuth } from '@/api'
import type { UserProfile } from '@/types/api'

export const useAuthStore = defineStore('auth', () => {
  const user = ref<UserProfile | null>(getUser())
  const token = ref<string | null>(localStorage.getItem('token'))
  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => user.value?.role === 'admin')

  function login(data: { user: UserProfile; token: string }) {
    setAuth(data.user, data.token)
    user.value = data.user
    token.value = data.token
  }

  function logout() {
    clearAuth()
    user.value = null
    token.value = null
  }

  function restore() {
    user.value = getUser()
    token.value = localStorage.getItem('token')
  }

  return { user, token, isLoggedIn, isAdmin, login, logout, restore }
})
