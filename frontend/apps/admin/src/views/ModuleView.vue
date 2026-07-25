<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import axios from 'axios'
import { useAuthStore } from '@utils/stores/auth'

const route = useRoute()
const moduleName = computed(() => route.meta.module as string)
const data = ref<any[]>([])
const loading = ref(true)
const error = ref('')

const moduleInfo: Record<string,{icon:string;label:string;desc:string;api:string}> = {
  account:   { icon:'👤', label:'帳號管理', desc:'用戶帳號、角色權限、實名認證', api:'/api/v1/account/list' },
  consult:   { icon:'📞', label:'問診管理', desc:'圖文/語音/視訊問診、醫生排班', api:'/api/v1/consult/rooms' },
  prescription:{icon:'💊',label:'處方管理', desc:'電子處方、配伍禁忌校驗、電子簽章', api:'/api/v1/prescription?page=1&size=20' },
  his:       { icon:'🏥', label:'診所門診', desc:'掛號分診、電子病歷、收費藥房', api:'/api/v1/his/hello' },
  inventory: { icon:'📦', label:'藥材庫存', desc:'藥材字典、採購管理、庫存鎖定、效期盤點', api:'/api/v1/inventory/health' },
  finance:   { icon:'💰', label:'財務結算', desc:'總帳分帳、退費冲帳、醫保對帳', api:'/api/v1/finance/health' },
  decoction: { icon:'🍵', label:'煎藥中心', desc:'煎藥訂單、批次追蹤、真空包裝', api:'/api/v1/decoction/hello' },
  logistics:{ icon:'🚚', label:'物流配送', desc:'自動打單、軌跡追蹤、簽收回調', api:'/api/v1/logistics/trace/TEST123' },
  insurance:{ icon:'🏦', label:'醫保對接', desc:'門診結算、處方/病歷上傳', api:'/api/v1/consult/health' },
  notify:   { icon:'🔔', label:'通知服務', desc:'Web Push、短信、院內看板通知', api:'/api/v1/notify/ping' },
  risk:     { icon:'🛡️', label:'風控管理', desc:'敏感詞過濾、異地登入偵測、防刷單', api:'/api/v1/risk/ping' },
  admin:    { icon:'⚙️', label:'系統管理', desc:'診所後台 + 平台後台、權限管理', api:'/api/v1/admin/dashboard' },
}

const info = computed(() => moduleInfo[moduleName.value] || { icon:'📄', label:moduleName.value, desc:'', api:'' })

onMounted(async () => {
  loading.value = true; error.value = ''
  try {
    const api = axios.create({ baseURL: '', timeout: 5000 })
    const token = useAuthStore().token
    if (token) api.defaults.headers.common.Authorization = `Bearer ${token}`
    const res = await api.get(info.value.api)
    data.value = Array.isArray(res.data) ? res.data : (res.data?.data?.records || res.data?.data || [res.data])
  } catch(e:any) { error.value = e.response?.status === 404 ? 'API 尚在開發中' : `加載失敗: ${e.message}` }
  finally { loading.value = false }
})
</script>

<template>
  <div>
    <div class="card">
      <h2>{{ info.icon }} {{ info.label }}</h2>
      <div class="text-sm" style="margin-top:.25rem">{{ info.desc }}</div>
    </div>

    <div v-if="loading" class="card" style="text-align:center">⏳ 載入中...</div>
    <div v-else-if="error" class="card" style="text-align:center;color:#e74c3c">⚠️ {{ error }}</div>
    <div v-else class="card">
      <pre style="font-size:.75rem;overflow-x:auto;max-height:400px">{{ JSON.stringify(data, null, 2) }}</pre>
    </div>
  </div>
</template>
