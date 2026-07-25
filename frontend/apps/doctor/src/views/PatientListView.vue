<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { patientApi } from '@utils/api'
import { useRouter } from 'vue-router'

const router = useRouter()
const patients = ref<any[]>([])
const keyword = ref('')
const loading = ref(false)

onMounted(search)
async function search() {
  loading.value = true
  try {
    const res = await patientApi.search(keyword.value)
    patients.value = res.data.data?.records || []
  } finally { loading.value = false }
}
</script>

<template>
  <div>
    <div class="card">
      <h2>👥 患者列表</h2>
      <div class="flex" style="margin-top:.5rem">
        <input v-model="keyword" placeholder="搜尋患者..." @keyup.enter="search" style="flex:1" />
        <button class="btn btn-primary" @click="search" :disabled="loading">🔍</button>
      </div>
    </div>

    <div v-for="p in patients" :key="p.id" class="card flex" style="justify-content:space-between">
      <div>
        <strong>{{ p.name }}</strong>
        <span class="text-sm" style="margin-left:.5rem">{{ p.gender === 'male' ? '男' : '女' }}</span>
        <div class="text-sm">{{ p.phone }} · {{ p.birthDate }}</div>
        <div v-if="p.allergyInfo" class="text-sm" style="color:#e74c3c">⚠ {{ p.allergyInfo }}</div>
      </div>
      <div style="display:flex;gap:.5rem">
        <button class="btn btn-outline" @click="router.push(`/emr/new/${p.id}`)">📋 新病歷</button>
        <button class="btn btn-accent" @click="router.push(`/prescription/new/${p.id}`)">💊 開處方</button>
      </div>
    </div>
  </div>
</template>
