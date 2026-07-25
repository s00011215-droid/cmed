<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
const router = useRouter()

const stats = [
  { icon:'👥', label:'今日門診', value:'12', color:'#2d6a4f' },
  { icon:'💊', label:'待配處方', value:'5', color:'#d4a373' },
  { icon:'💰', label:'今日營收', value:'HK$8,500', color:'#0984e3' },
  { icon:'📦', label:'庫存預警', value:'3', color:'#e74c3c' },
  { icon:'🔔', label:'待處理通知', value:'8', color:'#6c5ce7' },
  { icon:'🍵', label:'煎藥進行中', value:'4', color:'#00b894' },
]

const shortcuts = [
  { to:'/his', icon:'🏥', label:'掛號分診' },
  { to:'/prescription', icon:'💊', label:'處方管理' },
  { to:'/inventory', icon:'📦', label:'藥材庫存' },
  { to:'/finance', icon:'💰', label:'財務報表' },
]
</script>

<template>
  <h2 style="margin-bottom:1rem">📊 營運看板</h2>
  <div class="grid-3">
    <div v-for="s in stats" :key="s.label" class="card" style="text-align:center">
      <div style="font-size:2rem">{{ s.icon }}</div>
      <div style="font-size:1.5rem;font-weight:700;color:var(--primary)">{{ s.value }}</div>
      <div class="text-sm">{{ s.label }}</div>
    </div>
  </div>
  <h3 style="margin:1.5rem 0 .5rem">🔗 快捷操作</h3>
  <div class="grid-4">
    <div v-for="s in shortcuts" :key="s.label" class="card" style="text-align:center;cursor:pointer" @click="router.push(s.to)">
      <div style="font-size:2rem">{{ s.icon }}</div>
      <div style="font-weight:600">{{ s.label }}</div>
    </div>
  </div>
</template>
