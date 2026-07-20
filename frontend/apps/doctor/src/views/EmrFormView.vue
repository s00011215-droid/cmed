<script setup lang="ts">
import { ref } from 'vue'
import { emrApi } from '@utils/api'
import { useRouter } from 'vue-router'

const router = useRouter()
const form = ref({
  patientId: null as number | null,
  visitType: 'offline',
  chiefComplaint: '',
  presentIllness: '',
  detail: {
    inspection: { tongue: '', face: '', spirit: '神清' },
    auscultation: { voice: '語聲清晰', cough: '無', breath: '平穩' },
    inquiry: { appetite: '納可', sleep: '', stool: '便調', urine: '小便可' },
    palpation: { pulseLeft: '', pulseRight: '', pulseDetail: '' },
    tcmPattern: '',
    treatmentPrinciple: '',
  },
  diagnosis: '',
  advice: '',
})
const loading = ref(false)
const saved = ref<number | null>(null)

async function save() {
  if (!form.value.patientId) return alert('請輸入患者 ID')
  loading.value = true
  try {
    const res = await emrApi.save(form.value)
    saved.value = res.data.data
  } finally { loading.value = false }
}
</script>

<template>
  <div class="card">
    <h2 style="margin-bottom:1rem">📝 建立電子病歷</h2>

    <div class="grid-3">
      <div class="form-group">
        <label>患者 ID</label>
        <input v-model.number="form.patientId" type="number" placeholder="患者 ID" />
      </div>
      <div class="form-group">
        <label>就診類型</label>
        <select v-model="form.visitType">
          <option value="offline">線下門診</option>
          <option value="online">線上問診</option>
        </select>
      </div>
    </div>

    <div class="form-group"><label>主訴</label>
      <input v-model="form.chiefComplaint" placeholder="例如：失眠3個月，伴心悸、口苦" />
    </div>
    <div class="form-group"><label>現病史</label>
      <textarea v-model="form.presentIllness" placeholder="描述發病經過..." />
    </div>

    <h4 style="margin:1rem 0 .5rem">🔍 望診</h4>
    <div class="grid-3">
      <div class="form-group"><label>舌象</label><input v-model="form.detail.inspection.tongue" placeholder="舌紅，苔薄黃" /></div>
      <div class="form-group"><label>面色</label><input v-model="form.detail.inspection.face" placeholder="面色紅潤" /></div>
      <div class="form-group"><label>神態</label><input v-model="form.detail.inspection.spirit" placeholder="神清" /></div>
    </div>

    <h4 style="margin:1rem 0 .5rem">🫀 聞診 + 問診</h4>
    <div class="grid-3">
      <div class="form-group"><label>語聲</label><input v-model="form.detail.auscultation.voice" /></div>
      <div class="form-group"><label>食慾</label><input v-model="form.detail.inquiry.appetite" /></div>
      <div class="form-group"><label>睡眠</label><input v-model="form.detail.inquiry.sleep" placeholder="入睡困難，多夢" /></div>
      <div class="form-group"><label>大便</label><input v-model="form.detail.inquiry.stool" /></div>
      <div class="form-group"><label>小便</label><input v-model="form.detail.inquiry.urine" /></div>
    </div>

    <h4 style="margin:1rem 0 .5rem">✋ 切診</h4>
    <div class="grid-3">
      <div class="form-group"><label>左脈</label><input v-model="form.detail.palpation.pulseLeft" placeholder="弦" /></div>
      <div class="form-group"><label>右脈</label><input v-model="form.detail.palpation.pulseRight" placeholder="滑" /></div>
      <div class="form-group"><label>脈象詳述</label><input v-model="form.detail.palpation.pulseDetail" placeholder="左弦右滑" /></div>
    </div>

    <div class="grid-2">
      <div class="form-group"><label>辨證</label><input v-model="form.detail.tcmPattern" placeholder="肝鬱化火，擾動心神" /></div>
      <div class="form-group"><label>治則</label><input v-model="form.detail.treatmentPrinciple" placeholder="疏肝解鬱，清熱安神" /></div>
    </div>

    <div class="form-group"><label>診斷</label><input v-model="form.diagnosis" placeholder="不寐（肝鬱化火證）" /></div>
    <div class="form-group"><label>醫囑</label><textarea v-model="form.advice" placeholder="保持情緒穩定，避免咖啡濃茶..." /></div>

    <div class="flex" style="justify-content:flex-end;margin-top:1rem">
      <span v-if="saved" style="color:var(--primary);font-size:.875rem">✅ 已儲存 ID: {{ saved }}</span>
      <button class="btn btn-primary" @click="save" :disabled="loading">
        {{ loading ? '儲存中...' : '💾 儲存病歷' }}
      </button>
    </div>
  </div>
</template>
