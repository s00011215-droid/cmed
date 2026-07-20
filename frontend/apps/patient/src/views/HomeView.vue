<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { patientApi, prescriptionApi, decoctionApi, logisticsApi } from '@utils/api'

const patients = ref<any[]>([])
const keyword = ref('')
const loading = ref(false)

async function search() {
  loading.value = true
  try {
    const res = await patientApi.search(keyword.value)
    patients.value = res.data.data?.records || []
  } finally { loading.value = false }
}

onMounted(search)
</script>

<template>
  <div>
    <div class="card">
      <h2 style="margin-bottom:1rem">🏥 患者查詢</h2>
      <div class="flex">
        <div class="form-group" style="flex:1;margin:0">
          <input v-model="keyword" placeholder="輸入姓名或手機號搜尋..." @keyup.enter="search" />
        </div>
        <button class="btn btn-primary" @click="search" :disabled="loading">
          {{ loading ? '搜尋中...' : '🔍 搜尋' }}
        </button>
      </div>
    </div>

    <div v-for="p in patients" :key="p.id" class="card flex" style="justify-content:space-between">
      <div>
        <strong>{{ p.name }}</strong>
        <span class="text-sm" style="margin-left:.5rem">{{ p.gender === 'male' ? '男' : p.gender === 'female' ? '女' : '' }}</span>
        <div class="text-sm">{{ p.phone }}</div>
        <div class="text-sm">出生：{{ p.birthDate }}</div>
      </div>
      <router-link :to="`/patients/${p.id}`" class="btn btn-outline">查看</router-link>
    </div>

    <div v-if="!loading && patients.length === 0" class="card" style="text-align:center;color:#999">
      暫無患者資料
    </div>
  </div>
</template>
