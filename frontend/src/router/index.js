import { createRouter, createWebHashHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/LoginView.vue'),
    meta: { requiresAuth: false, title: '登录 - 菲林日记' },
  },
  {
    path: '/',
    component: () => import('@/components/layout/AppLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      { path: '', name: 'Home', component: () => import('@/views/HomeView.vue'), meta: { title: '菲林日记' } },
      { path: 'discover', name: 'Discover', component: () => import('@/views/DiscoverView.vue'), meta: { title: '发现 - 菲林日记' } },
      { path: 'records', name: 'Records', component: () => import('@/views/RecordsView.vue'), meta: { title: '观影记录 - 菲林日记' } },
      { path: 'reviews', name: 'Reviews', component: () => import('@/views/ReviewsView.vue'), meta: { title: '我的影评 - 菲林日记' } },
    ],
  },
  { path: '/:pathMatch(.*)*', redirect: '/' },
]

const router = createRouter({
  history: createWebHashHistory(),
  routes,
})

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  if (to.path === '/login') {
    next()
  } else if (to.meta.requiresAuth && !authStore.isLoggedIn) {
    next('/login')
  } else {
    next()
  }
})

router.afterEach((to) => {
  document.title = to.meta.title || '菲林日记'
})

export default router
