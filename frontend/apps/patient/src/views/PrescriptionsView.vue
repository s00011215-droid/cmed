<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { prescriptionApi } from '@utils/api'

const prescriptions = ref<any[]>([])
const loading = ref(true)

onMounted(async () => {
  try {
    const res = await prescriptionApi.listByPatient(356000139120218120)
    prescriptions.value = res.data.data?.records || res.data?.records || []
  } catch(e) { console.error(e) }
  finally { loading.value = false }
})

const labels: Record<string,string> = {
  draft:'草稿', submitted:'已提交', paid:'已付款',
  dispensed:'已配藥', decocting:'煎煮中', shipped:'已出貨', completed:'已完成'
}
</script>

<template>
  <div>
    <div class="card"><h2>💊 我的處方</h2></div>
    <div v-for="rx in prescriptions" :key="rx.id" class="card">
      <div class="flex" style="justify-content:space-between">
        <strong>{{ rx.prescriptionNo }}</strong>
        <span :class="`badge badge-paid`">{{ labels[rx.status] || rx.status }}</span>
      </div>
      <div class="text-sm">{{ rx.doseCount }} 劑 · HK${{ rx.totalAmount || '—' }}</div>
      <div class="text-sm">{{ new Date(rx.createdAt).toLocaleDateString('zh-HK') }}</div>
    </div>
    <div v-if="!loading && prescriptions.length === 0" class="card" style="text-align:center;color:#999">
      暫無處方記錄
    </div>
  </div>
</template>
