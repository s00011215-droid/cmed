<script setup lang="ts">
import { ref } from 'vue'
import { prescriptionApi } from '@utils/api'

const form = ref({
  patientId: null as number | null, doctorId: 1, visitType: 'offline',
  doseCount: 5, doseDays: 5, decoctionMethod: 'self', deliveryOption: 'pickup',
  items: [
    { materialId: 7, materialName: '柴胡', dosage: 12, unit: 'g', decoctionNote: '無', unitPrice: 0.65, subtotal: 0 },
    { materialId: 15, materialName: '龍骨', dosage: 30, unit: 'g', decoctionNote: '先煎', unitPrice: 0.80, subtotal: 0 },
    { materialId: 1, materialName: '黨參', dosage: 15, unit: 'g', decoctionNote: '無', unitPrice: 0.50, subtotal: 0 },
  ]
})
const result = ref<any>(null)
const loading = ref(false)

async function save() {
  if (!form.value.patientId) return alert('請輸入患者 ID')
  loading.value = true
  try {
    const items = form.value.items.map(i => ({ ...i, unitPrice: i.unitPrice, subtotal: i.dosage * i.unitPrice }))
    const res = await prescriptionApi.save({ ...form.value, items })
    result.value = res.data.data
  } finally { loading.value = false }
}

function addItem() {
  form.value.items.push({ materialId: 1, materialName: '', dosage: 0, unit: 'g', decoctionNote: '無', unitPrice: 0, subtotal: 0 })
}
function removeItem(i: number) { form.value.items.splice(i, 1) }
</script>

<template>
  <div class="card">
    <h2 style="margin-bottom:1rem">💊 開立處方</h2>

    <div class="grid-2">
      <div class="form-group"><label>患者 ID</label><input v-model.number="form.patientId" type="number" /></div>
      <div class="form-group"><label>劑數</label><input v-model.number="form.doseCount" type="number" min="1" /></div>
    </div>
    <div class="grid-2">
      <div class="form-group"><label>煎藥方式</label>
        <select v-model="form.decoctionMethod">
          <option value="self">自煎</option><option value="center">代煎</option>
        </select>
      </div>
      <div class="form-group"><label>配送方式</label>
        <select v-model="form.deliveryOption">
          <option value="pickup">自取</option><option value="delivery">配送</option>
        </select>
      </div>
    </div>

    <h4 style="margin:1rem 0 .5rem">藥材明細</h4>
    <div v-for="(item, i) in form.items" :key="i" class="flex" style="margin-bottom:.5rem;flex-wrap:wrap;gap:.5rem">
      <input v-model="item.materialName" placeholder="藥材名" style="width:100px" />
      <input v-model.number="item.dosage" type="number" placeholder="用量" style="width:60px" />
      <select v-model="item.decoctionNote" style="width:80px">
        <option>無</option><option>先煎</option><option>後下</option><option>烊化</option><option>包煎</option><option>冲服</option>
      </select>
      <input v-model.number="item.unitPrice" type="number" step="0.01" placeholder="單價" style="width:70px" />
      <span style="font-size:.75rem">小計: {{ (item.dosage * item.unitPrice).toFixed(2) }}</span>
      <button class="btn btn-danger" @click="removeItem(i)">✕</button>
    </div>
    <button class="btn" style="background:#eee;margin-bottom:1rem" @click="addItem">+ 新增藥材</button>

    <div style="text-align:right">
      <span v-if="result?.warnings?.length" style="color:#e67e22;margin-right:1rem">
        ⚠️ {{ result.warnings.join('; ') }}
      </span>
      <button class="btn btn-primary" @click="save" :disabled="loading">
        {{ loading ? '處理中...' : '📋 開立處方' }}
      </button>
    </div>

    <div v-if="result" style="margin-top:1rem;padding:1rem;background:#f0faf4;border-radius:8px">
      <strong>處方 {{ result.prescriptionNo }}</strong> · 狀態：{{ result.status }}
      <br/>總金額：HK${{ result.totalAmount }} · 劑數：{{ form.doseCount }}
      <div v-if="result.warnings?.length" style="color:#e67e22;margin-top:.5rem">
        ⚠️ 配伍警告：{{ result.warnings.join('; ') }}
      </div>
    </div>
  </div>
</template>
