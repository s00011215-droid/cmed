<script setup lang="ts">
import { ref } from 'vue'
import { accountApi } from '@utils/api'
import { useAuthStore } from '@utils/stores/auth'
import { useRouter } from 'vue-router'

const auth = useAuthStore()
const router = useRouter()
const username = ref('')
const password = ref('')
const error = ref('')
const loading = ref(false)

async function login() {
  console.log('login called', username.value, password.value)
  error.value = ''
  loading.value = true
  try {
    const res = await accountApi.login({ username: username.value, password: password.value })
    console.log('login response', res.data)
    const d = res.data.data
    auth.setAuth(d.accessToken, d.refreshToken, d.userInfo)
    router.push('/')
  } catch (e: any) {
    console.error('login error', e)
    error.value = e.response?.data?.message || '登入失敗'
  } finally { loading.value = false }
}
</script>

<template>
  <div style="max-width:400px;margin:4rem auto">
    <div class="card">
      <h2 style="text-align:center;margin-bottom:1.5rem">🌿 祥雲智方 · 登入</h2>
      <div v-if="error" style="color:#e74c3c;margin-bottom:1rem;font-size:.875rem">{{ error }}</div>
      <div class="form-group"><label>用戶名</label><input v-model="username" placeholder="輸入用戶名" /></div>
      <div class="form-group"><label>密碼</label><input v-model="password" type="password" placeholder="輸入密碼" @keyup.enter="login" /></div>
      <button class="btn btn-primary" style="width:100%" @click="login" :disabled="loading">
        {{ loading ? '登入中...' : '登入' }}
      </button>
      <div style="text-align:center;margin-top:1rem">
        <span class="text-sm">測試帳號：dr_chan / password123</span>
      </div>
    </div>
  </div>
</template>
