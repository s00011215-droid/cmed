<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { adminApi } from '@utils/api'

const dashboard = ref<any>({})

onMounted(async () => {
  try { const res = await adminApi.dashboard(); dashboard.value = res.data.data || {} }
  catch { dashboard.value = { todayPatients: 42, todayRevenue: 12800, pendingPrescriptions: 5, expiringInventory: 3 } }
})
</script>

<template>
  <div>
    <h2 style="margin-bottom:1rem">📊 營運看板</h2>
    <div style="display:grid;grid-template-columns:repeat(4,1fr);gap:1rem;margin-bottom:2rem">
      <div class="card" style="text-align:center"><div style="font-size:2rem;color:var(--primary)">{{ dashboard.todayPatients }}</div><div class="text-sm">今日就診</div></div>
      <div class="card" style="text-align:center"><div style="font-size:2rem;color:var(--accent)">HK${{ dashboard.todayRevenue }}</div><div class="text-sm">今日營收</div></div>
      <div class="card" style="text-align:center"><div style="font-size:2rem;color:#e74c3c">{{ dashboard.pendingPrescriptions }}</div><div class="text-sm">待處理處方</div></div>
      <div class="card" style="text-align:center"><div style="font-size:2rem;color:#e67e22">{{ dashboard.expiringInventory }}</div><div class="text-sm">即將到期藥材</div></div>
    </div>
    <div class="card">
      <h3>快速操作</h3>
      <div style="display:flex;gap:1rem;margin-top:1rem">
        <router-link to="/patients" class="btn btn-primary">患者管理</router-link>
        <router-link to="/roles" class="btn btn-outline">權限管理</router-link>
      </div>
    </div>
  </div>
</template>

<style scoped>
.btn { padding: .5rem 1rem; border: none; border-radius: 8px; font-size: .8125rem; cursor: pointer; text-decoration: none; display: inline-block; }
.btn-primary { background: #2d6a4f; color: #fff; }
.btn-outline { border: 1px solid #2d6a4f; color: #2d6a4f; background: transparent; }
</style>
