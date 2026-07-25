<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { patientApi, prescriptionApi } from '@utils/api'

const route = useRoute()
const router = useRouter()
const patient = ref<any>(null)
const loading = ref(false)
const herbList = ref<string[]>([])

const form = ref({
  patientId: Number(route.params.patientId),
  diagnosis: '',
  doseCount: 5,
  instructions: '每日一劑，水煎分兩次溫服',
  herbs: [] as { name: string; dosage: string; unit: string }[],
})

const knownHerbs = ['黃連','黃芩','黃柏','大黃','人參','當歸','川芎','白芍','熟地','茯苓',
  '白朮','甘草','半夏','陳皮','枳殼','柴胡','香附','鬱金','酸棗仁','遠志',
  '龍骨','牡蠣','桂枝','生薑','大棗','丹參','桃仁','紅花','澤瀉','車前子',
  '金銀花','連翹','菊花','薄荷','桑葉','桔梗','杏仁','貝母','瓜蔞']

function addHerb() { form.value.herbs.push({ name: '', dosage: '', unit: 'g' }) }
function removeHerb(i: number) { form.value.herbs.splice(i, 1) }

const total = computed(() => form.value.herbs.reduce((s,h) => s + (parseFloat(h.dosage)||0), 0))

onMounted(async () => {
  const res = await patientApi.get(form.value.patientId)
  patient.value = res.data.data
  addHerb()
})

async function submit() {
  loading.value = true
  try {
    const items = form.value.herbs.map(h => ({
      herbName: h.name, dosage: parseFloat(h.dosage) || 0, unit: h.unit
    }))
    await prescriptionApi.save({
      patientId: form.value.patientId,
      diagnosis: form.value.diagnosis,
      items,
      doseCount: form.value.doseCount,
      instructions: form.value.instructions,
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
      <h2>💊 開立處方</h2>
      <div v-if="patient" class="text-sm">患者：{{ patient.name }} · {{ patient.phone }}</div>
    </div>

    <div class="card">
      <div class="form-group"><label>中醫辨證</label><input v-model="form.diagnosis" placeholder="肝鬱化火，擾動心神" /></div>
      <div class="grid-2">
        <div class="form-group"><label>劑數</label><input v-model.number="form.doseCount" type="number" min="1" max="30" /></div>
      </div>
      <div class="form-group"><label>服法</label><input v-model="form.instructions" /></div>
    </div>

    <div class="card">
      <h3>🌿 藥材明細 <span class="text-sm">（總量：{{ total }}g）</span></h3>
      <div v-for="(h, i) in form.herbs" :key="i" class="flex" style="margin-top:.5rem;gap:.25rem">
        <input v-model="h.name" :list="'herbs-list'" placeholder="藥名" style="flex:2" />
        <datalist id="herbs-list"><option v-for="k in knownHerbs" :key="k" :value="k" /></datalist>
        <input v-model="h.dosage" placeholder="劑量" type="number" min="0.1" step="0.1" style="flex:1" />
        <span>g</span>
        <button class="btn btn-outline" @click="removeHerb(i)" style="padding:.25rem .5rem">✕</button>
      </div>
      <button class="btn btn-outline" style="margin-top:.75rem" @click="addHerb">＋ 添加藥材</button>
    </div>

    <button class="btn btn-accent" style="width:100%;margin-top:1rem" @click="submit" :disabled="loading">
      {{ loading ? '儲存中...' : '📝 開立處方' }}
    </button>
  </div>
</template>
