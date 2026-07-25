<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { patientApi, emrApi } from '@utils/api'

const route = useRoute()
const router = useRouter()
const patient = ref<any>(null)
const loading = ref(false)

const form = ref({
  patientId: Number(route.params.patientId),
  visitType: 'offline',
  chiefComplaint: '',
  presentIllness: '',
  pastHistory: '',
  inspection_tongue: '',
  inspection_face: '',
  inspection_spirit: '',
  auscultation_voice: '',
  auscultation_breath: '',
  inquiry_appetite: '',
  inquiry_sleep: '',
  inquiry_stool: '',
  inquiry_urine: '',
  palpation_pulseLeft: '',
  palpation_pulseRight: '',
  diagnosis: '',
  treatmentPrinciple: '',
  advice: '',
  temperature: undefined as number | undefined,
  bloodPressure: '',
  heartRate: undefined as number | undefined,
})

onMounted(async () => {
  const res = await patientApi.get(form.value.patientId)
  patient.value = res.data.data
})

async function submit() {
  loading.value = true
  try {
    const detail: any = {
      inspection: { tongue: form.value.inspection_tongue, face: form.value.inspection_face, spirit: form.value.inspection_spirit },
      auscultation: { voice: form.value.auscultation_voice, breath: form.value.auscultation_breath },
      inquiry: { appetite: form.value.inquiry_appetite, sleep: form.value.inquiry_sleep, stool: form.value.inquiry_stool, urine: form.value.inquiry_urine },
      palpation: { pulseLeft: form.value.palpation_pulseLeft, pulseRight: form.value.palpation_pulseRight },
      tcm_pattern: form.value.diagnosis,
      treatment_principle: form.value.treatmentPrinciple,
    }
    await emrApi.save({
      patientId: form.value.patientId,
      visitType: form.value.visitType,
      chiefComplaint: form.value.chiefComplaint,
      presentIllness: form.value.presentIllness,
      pastHistory: form.value.pastHistory,
      detail,
      diagnosis: form.value.diagnosis,
      advice: form.value.advice,
    })
    router.push('/')
  } catch (e: any) {
    alert('儲存失敗：' + (e.response?.data?.message || e.message))
  } finally { loading.value = false }
}
</script>

<template>
  <div>
    <div class="card">
      <h2>📋 新病歷</h2>
      <div v-if="patient" class="text-sm">患者：{{ patient.name }} · {{ patient.gender === 'male' ? '男' : '女' }} · {{ patient.phone }}</div>
      <div v-if="patient?.allergyInfo" class="text-sm" style="color:#e74c3c;margin-top:.25rem">⚠ 過敏史：{{ patient.allergyInfo }}</div>
    </div>

    <div class="card">
      <h3>基本資訊</h3>
      <div class="grid-2" style="margin-top:.5rem">
        <div class="form-group">
          <label>就診方式</label>
          <select v-model="form.visitType"><option value="offline">線下門診</option><option value="online">線上視訊</option></select>
        </div>
      </div>
      <div class="form-group"><label>主訴</label><textarea v-model="form.chiefComplaint" placeholder="患者主要不適..." /></div>
      <div class="form-group"><label>現病史</label><textarea v-model="form.presentIllness" placeholder="發病經過..." /></div>
      <div class="form-group"><label>既往史</label><textarea v-model="form.pastHistory" placeholder="既往病史..." /></div>
    </div>

    <div class="card">
      <h3>🔍 望診</h3>
      <div class="grid-3">
        <div class="form-group"><label>舌象</label><input v-model="form.inspection_tongue" placeholder="舌紅，苔薄黃" /></div>
        <div class="form-group"><label>面色</label><input v-model="form.inspection_face" placeholder="面色偏紅" /></div>
        <div class="form-group"><label>神態</label><input v-model="form.inspection_spirit" placeholder="神清" /></div>
      </div>
    </div>

    <div class="card">
      <h3>👂 聞診</h3>
      <div class="grid-2">
        <div class="form-group"><label>語聲</label><input v-model="form.auscultation_voice" placeholder="語聲洪亮" /></div>
        <div class="form-group"><label>呼吸</label><input v-model="form.auscultation_breath" placeholder="平穩" /></div>
      </div>
    </div>

    <div class="card">
      <h3>❓ 問診</h3>
      <div class="grid-2">
        <div class="form-group"><label>食慾</label><input v-model="form.inquiry_appetite" placeholder="納可" /></div>
        <div class="form-group"><label>睡眠</label><input v-model="form.inquiry_sleep" placeholder="入睡困難" /></div>
        <div class="form-group"><label>大便</label><input v-model="form.inquiry_stool" placeholder="偏乾" /></div>
        <div class="form-group"><label>小便</label><input v-model="form.inquiry_urine" placeholder="黃" /></div>
      </div>
    </div>

    <div class="card">
      <h3>🤚 切診</h3>
      <div class="grid-2">
        <div class="form-group"><label>左脈</label><input v-model="form.palpation_pulseLeft" placeholder="弦數" /></div>
        <div class="form-group"><label>右脈</label><input v-model="form.palpation_pulseRight" placeholder="弦" /></div>
      </div>
    </div>

    <div class="card">
      <h3>📝 診斷與處置</h3>
      <div class="form-group"><label>中醫辨證</label><input v-model="form.diagnosis" placeholder="肝鬱化火，擾動心神" /></div>
      <div class="form-group"><label>治則</label><input v-model="form.treatmentPrinciple" placeholder="疏肝解鬱，清熱安神" /></div>
      <div class="form-group"><label>醫囑</label><textarea v-model="form.advice" placeholder="注意事項..." /></div>
      <button class="btn btn-primary" style="width:100%" @click="submit" :disabled="loading">{{ loading ? '儲存中...' : '💾 儲存病歷' }}</button>
    </div>
  </div>
</template>
