const BASE = '/api'

interface ApiOptions { method?: string; body?: any; auth?: boolean }

export async function request<T>(url: string, opts: ApiOptions = {}): Promise<T> {
  const headers: Record<string, string> = {}
  if (opts.auth) {
    const token = localStorage.getItem('token')
    if (token) headers['Authorization'] = `Bearer ${token}`
  }
  if (opts.body && !(opts.body instanceof FormData)) {
    headers['Content-Type'] = 'application/json'
  }
  const res = await fetch(`${BASE}${url}`, {
    method: opts.method || (opts.body ? 'POST' : 'GET'),
    headers,
    body: opts.body ? JSON.stringify(opts.body) : undefined
  })
  if (!res.ok) {
    const err = await res.json().catch(() => ({ error: res.statusText }))
    throw new Error(err.error || 'Request failed')
  }
  return res.json()
}

export const api = {
  get: <T>(url: string) => request<T>(url, { auth: true }),
  post: <T>(url: string, body?: any) => request<T>(url, { method: 'POST', body, auth: true }),
  put: <T>(url: string, body?: any) => request<T>(url, { method: 'PUT', body, auth: true }),
  delete: <T>(url: string) => request<T>(url, { method: 'DELETE', auth: true }),
  login: (username: string, password: string) =>
    request<{ user: any; token: string }>('/auth/login', { method: 'POST', body: { username, password } }),
  register: (data: any) =>
    request<{ user: any; token: string }>('/auth/register', { method: 'POST', body: data })
}

export function setAuth(user: any, token: string) {
  localStorage.setItem('user', JSON.stringify(user))
  localStorage.setItem('token', token)
}
export function clearAuth() { localStorage.removeItem('user'); localStorage.removeItem('token') }
export function getUser(): any {
  try { const u = localStorage.getItem('user'); return u ? JSON.parse(u) : null } catch { return null }
}
