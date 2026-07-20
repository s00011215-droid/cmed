<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { patientApi, emrApi, prescriptionApi } from '@utils/api'

const route = useRoute()
const patient = ref<any>(null)
const emrs = ref<any[]>([])
const prescriptions = ref<any[]>([])
const loading = ref(true)

onMounted(async () => {
  const id = Number(route.params.id)
  try {
    const [pRes, eRes, rxRes] = await Promise.all([
      patientApi.get(id),
      emrApi.listByPatient(id),
      prescriptionApi.listByPatient(id)
    ])
    patient.value = pRes.data.data
    emrs.value = eRes.data.data?.records || []
    prescriptions.value = rxRes.data.data?.records || []
  } finally { loading.value = false }
})

const statusBadge = (s: string) => `badge badge-${s}`
</script>

<template>
  <div v-if="loading" style="text-align:center;padding:2rem">載入中...</div>
  <div v-else-if="patient">
    <div class="card">
      <h2>{{ patient.name }}</h2>
      <div class="grid-2" style="margin-top:.5rem">
        <div><span class="text-sm">性別</span><br/>{{ patient.gender === 'male' ? '男' : '女' }}</div>
        <div><span class="text-sm">電話</span><br/>{{ patient.phone }}</div>
        <div><span class="text-sm">出生日期</span><br/>{{ patient.birthDate }}</div>
        <div><span class="text-sm">血型</span><br/>{{ patient.bloodType || '-' }}</div>
      </div>
      <div v-if="patient.allergyInfo" style="margin-top:.75rem;padding:.5rem;background:#fff3cd;border-radius:8px">
        ⚠️ 過敏史：{{ patient.allergyInfo }}
      </div>
    </div>

    <h3 style="margin-bottom:.5rem">📋 病歷記錄</h3>
    <div v-for="e in emrs" :key="e.id" class="card">
      <div class="flex" style="justify-content:space-between">
        <strong>{{ new Date(e.createdAt).toLocaleDateString('zh-HK') }}</strong>
        <span :class="statusBadge(e.visitType)">{{ e.visitType }}</span>
      </div>
      <div style="margin-top:.5rem"><span class="text-sm">主訴：</span>{{ e.chiefComplaint }}</div>
      <div class="text-sm">診斷：{{ e.diagnosis }}</div>
    </div>

    <h3 style="margin-bottom:.5rem">💊 處方記錄</h3>
    <div v-for="rx in prescriptions" :key="rx.id" class="card">
      <div class="flex" style="justify-content:space-between">
        <strong>{{ rx.prescriptionNo }}</strong>
        <span :class="`badge badge-${rx.status}`">{{ rx.status }}</span>
      </div>
      <div class="text-sm">劑數：{{ rx.doseCount }} 劑 · 金額：HK${{ rx.totalAmount }}</div>
      <div class="text-sm">{{ new Date(rx.createdAt).toLocaleDateString('zh-HK') }}</div>
    </div>
  </div>
</template>
