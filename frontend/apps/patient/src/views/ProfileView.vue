<script setup lang="ts">
import { ref } from 'vue'
import { useAuthStore } from '@utils/stores/auth'
const auth = useAuthStore()
const user = ref(auth.userInfo || { name: '陳大文', role: 'doctor' })
</script>

<template>
  <div>
    <div class="card">
      <h2>👤 個人資料</h2>
      <div class="grid-2" style="margin-top:1rem">
        <div><span class="text-sm">用戶名</span><br/><strong>{{ user.username || 'dr_chan' }}</strong></div>
        <div><span class="text-sm">角色</span><br/>{{ user.role === 'doctor' ? '醫師' : user.role === 'admin' ? '管理員' : '患者' }}</div>
        <div><span class="text-sm">姓名</span><br/>{{ user.name || '陳大文' }}</div>
        <div><span class="text-sm">診所</span><br/>祥雲中醫診所</div>
      </div>
    </div>
    <div class="card">
      <h2>🔐 帳號安全</h2>
      <button class="btn btn-outline" style="margin-top:.5rem" @click="auth.logout();$router.push('/login')">登出</button>
    </div>
  </div>
</template>
