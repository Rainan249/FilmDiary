<template>
  <div class="login-page">
    <div class="atmosphere"></div>
    <div class="texture"></div>
    <div class="login-container">
      <div class="login-card">
        <div class="logo">
          <div class="logo-mark">🎬</div>
          <div class="logo-text">菲林日记</div>
        </div>
        <div class="logo-sub">Film Diary</div>
        <div class="hint-text">
          默认账号 <strong>root</strong>，密码 <strong>123456</strong>
        </div>

        <div v-if="errorMsg" class="error-message show">{{ errorMsg }}</div>

        <form @submit.prevent="handleLogin" autocomplete="off">
          <div class="form-group">
            <label class="form-label">账号</label>
            <div class="input-wrapper">
              <input type="text" class="form-input" v-model="username" placeholder="请输入账号" required autocomplete="username">
              <span class="input-icon">👤</span>
            </div>
          </div>
          <div class="form-group">
            <label class="form-label">密码</label>
            <div class="input-wrapper">
              <input :type="showPassword ? 'text' : 'password'" class="form-input" v-model="password" placeholder="请输入密码" required autocomplete="current-password">
              <span class="input-icon">🔒</span>
              <button type="button" class="toggle-password" @click="showPassword = !showPassword" tabindex="-1">{{ showPassword ? '🙈' : '👁' }}</button>
            </div>
          </div>
          <div class="form-options">
            <label class="remember-me">
              <input type="checkbox" v-model="rememberMe"> 记住我
            </label>
          </div>
          <button type="submit" class="login-btn" :disabled="loading">
            <span v-if="!loading" class="btn-text">登 录</span>
            <div v-else class="spinner"></div>
          </button>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const username = ref('')
const password = ref('')
const showPassword = ref(false)
const rememberMe = ref(false)
const loading = ref(false)
const errorMsg = ref('')

onMounted(() => {
  const savedRemember = localStorage.getItem('rememberMe') === 'true'
  if (savedRemember) {
    rememberMe.value = true
    const savedUsername = localStorage.getItem('savedUsername')
    const savedPassword = localStorage.getItem('savedPassword')
    if (savedUsername) username.value = savedUsername
    if (savedPassword) password.value = savedPassword
  } else {
    const savedUsername = localStorage.getItem('savedUsername')
    if (savedUsername) username.value = savedUsername
  }
})

async function handleLogin() {
  errorMsg.value = ''
  if (!username.value.trim()) { errorMsg.value = '请输入账号'; return }
  if (!password.value.trim()) { errorMsg.value = '请输入密码'; return }

  loading.value = true
  try {
    const result = await authStore.login(username.value.trim(), password.value.trim())
    if (result.success) {
      if (rememberMe.value) {
        localStorage.setItem('rememberMe', 'true')
        localStorage.setItem('savedUsername', username.value.trim())
        localStorage.setItem('savedPassword', password.value.trim())
      } else {
        localStorage.removeItem('rememberMe')
        localStorage.removeItem('savedUsername')
        localStorage.removeItem('savedPassword')
      }
      router.replace('/')
    } else {
      errorMsg.value = result.message
      password.value = ''
    }
  } catch (e) {
    errorMsg.value = '网络错误，请稍后重试'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  position: fixed;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  background: var(--bg-base);
  z-index: 100;
}
</style>

<style>
.login-page .atmosphere {
  position: fixed;
  inset: 0;
  z-index: 0;
  pointer-events: none;
}

.login-page .atmosphere::before {
  content: '';
  position: absolute;
  top: -15%;
  right: -5%;
  width: 60vw;
  height: 60vh;
  background: radial-gradient(ellipse, rgba(212, 168, 83, 0.15) 0%, transparent 60%);
  animation: drift 22s ease-in-out infinite alternate;
}

.login-page .atmosphere::after {
  content: '';
  position: absolute;
  bottom: -10%;
  left: 5%;
  width: 50vw;
  height: 50vh;
  background: radial-gradient(ellipse, rgba(180, 160, 140, 0.12) 0%, transparent 60%);
  animation: drift 28s ease-in-out infinite alternate-reverse;
}

@keyframes drift {
  from { transform: translate(0, 0) scale(1); }
  to { transform: translate(20px, -15px) scale(1.04); }
}

.login-page .texture {
  position: fixed;
  inset: 0;
  z-index: 0;
  pointer-events: none;
  opacity: 0.03;
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 256 256' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='n'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.85' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23n)'/%3E%3C/svg%3E");
  background-size: 128px 128px;
}

.login-container {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 420px;
  padding: 20px;
}

.login-card {
  background: var(--bg-card);
  backdrop-filter: blur(24px) saturate(1.3);
  -webkit-backdrop-filter: blur(24px) saturate(1.3);
  border: 1px solid var(--frost-border);
  border-radius: 24px;
  padding: 48px 40px;
  box-shadow: var(--shadow-lg);
  position: relative;
  overflow: hidden;
  animation: fadeIn 0.6s ease forwards;
}

.login-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, transparent, var(--accent), transparent);
}

.logo {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 14px;
  margin-bottom: 8px;
}

.logo-mark {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  background: linear-gradient(135deg, var(--accent-light) 0%, var(--accent) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26px;
  box-shadow: 0 4px 16px rgba(184, 134, 11, 0.25);
}

.logo-text {
  font-family: var(--font-display);
  font-size: 28px;
  font-weight: 700;
  color: var(--ink);
}

.logo-sub {
  font-size: 13px;
  color: var(--ink-muted);
  letter-spacing: 0.1em;
  text-transform: uppercase;
  text-align: center;
  margin-bottom: 36px;
}

.form-group { margin-bottom: 20px; }

.form-label {
  display: block;
  font-size: 14px;
  font-weight: 600;
  color: var(--ink-secondary);
  margin-bottom: 8px;
  letter-spacing: 0.02em;
}

.input-wrapper { position: relative; }

.form-input {
  width: 100%;
  padding: 14px 18px 14px 44px;
  background: var(--frost);
  border: 1.5px solid var(--frost-border);
  border-radius: 12px;
  font-size: 15px;
  font-family: var(--font-body);
  color: var(--ink);
  transition: all 0.3s ease;
  outline: none;
}

.form-input:focus {
  border-color: var(--accent);
  box-shadow: 0 0 0 3px rgba(184, 134, 11, 0.1);
  background: rgba(255, 255, 255, 0.85);
}

.form-input::placeholder { color: var(--ink-muted); }

.input-icon {
  position: absolute;
  left: 16px;
  top: 50%;
  transform: translateY(-50%);
  color: var(--ink-muted);
  font-size: 16px;
  pointer-events: none;
  transition: color 0.2s;
}

.form-input:focus ~ .input-icon { color: var(--accent); }

.toggle-password {
  position: absolute;
  right: 14px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  cursor: pointer;
  color: var(--ink-muted);
  font-size: 18px;
  padding: 4px 8px;
  transition: color 0.2s;
}

.toggle-password:hover { color: var(--accent); }

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.remember-me {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-size: 14px;
  color: var(--ink-secondary);
  user-select: none;
}

.remember-me input[type="checkbox"] {
  width: 16px;
  height: 16px;
  accent-color: var(--accent);
  cursor: pointer;
}

.login-btn {
  width: 100%;
  padding: 16px 24px;
  background: linear-gradient(135deg, var(--accent-light) 0%, var(--accent) 100%);
  border: none;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  font-family: var(--font-body);
  color: #fff;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 20px rgba(184, 134, 11, 0.3);
  position: relative;
  overflow: hidden;
}

.login-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 28px rgba(184, 134, 11, 0.4);
}

.login-btn:active:not(:disabled) { transform: translateY(0); }
.login-btn:disabled { opacity: 0.7; cursor: not-allowed; }

.hint-text {
  margin-top: 12px;
  padding: 14px 18px;
  background: rgba(212, 168, 83, 0.08);
  border: 1px dashed rgba(212, 168, 83, 0.3);
  border-radius: 10px;
  font-size: 15px;
  color: var(--ink-muted);
  text-align: center;
}

.hint-text strong {
  color: var(--accent);
  font-weight: 600;
}

.login-btn .spinner {
  display: none;
  width: 20px;
  height: 20px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin: 0 auto;
}

.login-btn.loading .btn-text { display: none; }
.login-btn.loading .spinner { display: block; }

@keyframes spin { to { transform: rotate(360deg); } }

.error-message {
  display: none;
  padding: 12px 16px;
  background: rgba(220, 53, 69, 0.1);
  border: 1px solid rgba(220, 53, 69, 0.2);
  border-radius: 10px;
  color: #dc3545;
  font-size: 14px;
  margin-bottom: 20px;
  text-align: center;
  animation: shake 0.4s ease;
}

.error-message.show { display: block; }

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-5px); }
  75% { transform: translateX(5px); }
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

@media (max-width: 480px) {
  .login-card { padding: 36px 24px; }
  .logo-mark { width: 44px; height: 44px; font-size: 22px; }
  .logo-text { font-size: 24px; }
}
</style>
