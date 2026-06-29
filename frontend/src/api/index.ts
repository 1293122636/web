import axios from 'axios'
import type { LoginResponse, UserProfile } from '@/types/api'

const api = axios.create({ baseURL: '/api' })

api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

api.interceptors.response.use(
  res => res,
  err => {
    const msg = err.response?.data?.error || err.message || 'Request failed'
    return Promise.reject(new Error(msg))
  }
)

export default api

export function setAuth(user: UserProfile, token: string) {
  localStorage.setItem('user', JSON.stringify(user))
  localStorage.setItem('token', token)
}
export function clearAuth() {
  localStorage.removeItem('user')
  localStorage.removeItem('token')
}
export function getUser(): UserProfile | null {
  try {
    const u = localStorage.getItem('user')
    return u ? JSON.parse(u) : null
  } catch { return null }
}

// Auth
export function doLogin(username: string, password: string) {
  return api.post<LoginResponse>('/auth/login', { username, password })
}
export function doRegister(data: { username: string; password: string; name: string; phone?: string; email?: string }) {
  return api.post<LoginResponse>('/auth/register', data)
}
export function getMe() { return api.get<UserProfile>('/auth/me') }

export { bookApi } from './books'
