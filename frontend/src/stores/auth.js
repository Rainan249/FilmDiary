import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'

export const useAuthStore = defineStore('auth', () => {
  const router = useRouter()
  const isLoggedIn = ref(false)
  const username = ref('')

  const isAuthenticated = computed(() => isLoggedIn.value)

  async function login(user, password) {
    const res = await fetch('/api/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username: user, password }),
    })
    const data = await res.json()
    if (data.success) {
      isLoggedIn.value = true
      username.value = user
      return { success: true }
    }
    return { success: false, message: data.message || '账号或密码错误' }
  }

  function logout() {
    isLoggedIn.value = false
    username.value = ''
    localStorage.removeItem('rememberMe')
    localStorage.removeItem('savedUsername')
    localStorage.removeItem('savedPassword')
    router.push('/login')
  }

  return { isLoggedIn, username, isAuthenticated, login, logout }
})
