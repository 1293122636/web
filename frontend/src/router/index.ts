import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { getUser } from '../api'

const routes = [
  { path: '/', redirect: '/books' },
  { path: '/login', name: 'Login', component: () => import('../views/Login.vue') },
  { path: '/books', name: 'Search', component: () => import('../views/public/Search.vue') },
  { path: '/books/:id', name: 'BookDetail', component: () => import('../views/public/BookDetail.vue') },
  {
    path: '/admin',
    component: () => import('../views/admin/Layout.vue'),
    meta: { role: 'admin' },
    children: [
      { path: '', redirect: '/admin/dashboard' },
      { path: 'dashboard', component: () => import('../views/admin/Dashboard.vue') },
      { path: 'books', component: () => import('../views/admin/Books.vue') },
      { path: 'categories', component: () => import('../views/admin/Categories.vue') },
      { path: 'readers', component: () => import('../views/admin/Readers.vue') },
      { path: 'borrows', component: () => import('../views/admin/Borrows.vue') },
      { path: 'fines', component: () => import('../views/admin/Fines.vue') },
      { path: 'stats', component: () => import('../views/admin/Stats.vue') },
      { path: 'settings', component: () => import('../views/admin/Settings.vue') },
      { path: 'circulation', component: () => import('../views/admin/Circulation.vue') }
    ]
  },
  {
    path: '/reader',
    component: () => import('../views/reader/Layout.vue'),
    meta: { role: 'reader' },
    children: [
      { path: '', redirect: '/reader/books' },
      { path: 'books', component: () => import('../views/reader/Books.vue') },
      { path: 'my-borrows', component: () => import('../views/reader/MyBorrows.vue') },
      { path: 'profile', component: () => import('../views/reader/Profile.vue') }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/books' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, _from, next) => {
  const auth = useAuthStore()
  // Prefer store state, fall back to localStorage (for page refresh)
  const user = auth.user || getUser()
  const publicRouteNames = ['Login', 'Search', 'BookDetail']
  if (to.name && publicRouteNames.includes(to.name as string)) {
    if (to.path === '/login' && user) {
      next(user.role === 'admin' ? '/admin/dashboard' : '/reader/books')
    } else {
      next()
    }
    return
  }
  if (!user) {
    auth.logout()  // Clears both store + localStorage
    next('/login')
    return
  }
  if (to.meta.role && to.meta.role !== user.role) {
    next(user.role === 'admin' ? '/admin/dashboard' : '/reader/books')
    return
  }
  next()
})

export default router
