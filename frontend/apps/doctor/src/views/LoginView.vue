<script setup lang="ts">
import { ref } from 'vue'
import { accountApi } from '@utils/api'
import { useAuthStore } from '@utils/stores/auth'
import { useRouter } from 'vue-router'

const auth = useAuthStore()
const router = useRouter()
const username = ref('dr_chan')
const password = ref('password123')
const error = ref('')
const loading = ref(false)

async function login() {
  error.value = ''; loading.value = true
  try {
    const res = await accountApi.login({ username: username.value, password: password.value })
    auth.setAuth(res.data.data.accessToken, res.data.data.refreshToken, res.data.data.userInfo)
    router.push('/')
  } catch (e: any) {
    error.value = e.response?.data?.message || '登入失敗'
  } finally { loading.value = false }
}
</script>

<template>
  <div style="max-width:400px;margin:4rem auto">
    <div class="card">
      <h2 style="text-align:center;margin-bottom:1.5rem">🩺 醫生登入</h2>
      <div v-if="error" style="color:#e74c3c;margin-bottom:1rem;font-size:.875rem">{{ error }}</div>
      <div class="form-group"><label>用戶名</label><input v-model="username" placeholder="輸入用戶名" /></div>
      <div class="form-group"><label>密碼</label><input v-model="password" type="password" placeholder="輸入密碼" @keyup.enter="login" /></div>
      <button class="btn btn-primary" style="width:100%" @click="login" :disabled="loading">{{ loading ? '登入中...' : '登入' }}</button>
    </div>
  </div>
</template>
