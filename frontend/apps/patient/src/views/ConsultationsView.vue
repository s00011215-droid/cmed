<script setup lang="ts">
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { useAuthStore } from '@utils/stores/auth'

const emrs = ref<any[]>([])
const loading = ref(true)

onMounted(async () => {
  const api = axios.create({ baseURL: 'http://localhost:8080', timeout: 10000 })
  const token = useAuthStore().token
  if (token) api.defaults.headers.common.Authorization = `Bearer ${token}`
  try {
    const res = await api.get('/api/v1/emr/patient/356000139120218120', { params: { page: 1 } })
    emrs.value = res.data.data?.records || res.data?.records || []
  } catch(e: any) {
    console.error('EMR load failed:', e.response?.status, e.message)
  }
  loading.value = false
})
</script>

<template>
  <div>
    <div class="card"><h2>📋 問診記錄</h2></div>
    <div v-if="loading" class="card" style="text-align:center">載入中...</div>
    <div v-for="e in emrs" :key="e.id" class="card">
      <div class="flex" style="justify-content:space-between">
        <strong>{{ new Date(e.createdAt).toLocaleDateString('zh-HK') }}</strong>
        <span class="badge badge-info">{{ e.visitType }}</span>
      </div>
      <div style="margin-top:.25rem">{{ e.chiefComplaint?.slice(0, 60) }}{{ e.chiefComplaint?.length > 60 ? '...' : '' }}</div>
      <div class="text-sm">診斷：{{ e.diagnosis }}</div>
    </div>
    <div v-if="!loading && emrs.length === 0" class="card" style="text-align:center;color:#999">暫無記錄</div>
  </div>
</template>
