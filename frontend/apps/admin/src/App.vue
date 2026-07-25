<script setup lang="ts">
import { useRouter, useRoute } from 'vue-router'
const router = useRouter()
const route = useRoute()

const navItems = [
  { path:'/', icon:'📊', name:'營運看板' },
  { path:'/account', icon:'👤', name:'帳號管理' },
  { path:'/consult', icon:'📞', name:'問診管理' },
  { path:'/prescription', icon:'💊', name:'處方管理' },
  { path:'/his', icon:'🏥', name:'診所門診' },
  { path:'/inventory', icon:'📦', name:'藥材庫存' },
  { path:'/finance', icon:'💰', name:'財務結算' },
  { path:'/decoction', icon:'🍵', name:'煎藥中心' },
  { path:'/logistics', icon:'🚚', name:'物流配送' },
  { path:'/insurance', icon:'🏦', name:'醫保對接' },
  { path:'/notify', icon:'🔔', name:'通知服務' },
  { path:'/risk', icon:'🛡️', name:'風控管理' },
  { path:'/admin', icon:'⚙️', name:'系統管理' },
]
</script>

<template>
  <div style="display:flex;min-height:100vh">
    <aside style="width:220px;background:var(--primary);color:#fff;padding:1rem 0;flex-shrink:0">
      <h1 style="font-size:1rem;padding:0 1rem 1rem;border-bottom:1px solid rgba(255,255,255,.2)">🌿 祥雲智方 · 後台</h1>
      <nav style="margin-top:.5rem">
        <router-link v-for="n in navItems" :key="n.path" :to="n.path"
          :style="{display:'block',padding:'.5rem 1rem',color:route.path===n.path?'#fff':'rgba(255,255,255,.75)',textDecoration:'none',fontSize:'.8125rem',background:route.path===n.path?'rgba(255,255,255,.15)':'transparent'}">
          {{ n.icon }} {{ n.name }}
        </router-link>
      </nav>
    </aside>
    <main style="flex:1;padding:1.5rem;background:var(--bg);min-height:100vh">
      <router-view />
    </main>
  </div>
</template>

<style>
:root { --primary: #2d6a4f; --accent: #d4a373; --bg: #f0f2f5; --card: #fff; --text: #333; --border: #e0e0e0; }
* { box-sizing: border-box; margin: 0; padding: 0; }
body { font-family: 'Noto Sans HK', -apple-system, sans-serif; color: var(--text); }
.card { background: var(--card); border-radius: 12px; padding: 1.25rem; margin-bottom: 1rem; box-shadow: 0 1px 4px rgba(0,0,0,.06); }
.btn { display: inline-flex; align-items: center; gap: .5rem; padding: .625rem 1.25rem; border: none; border-radius: 8px; font-size: .875rem; font-weight: 500; cursor: pointer; }
.btn-primary { background: var(--primary); color: #fff; }
.btn-accent { background: var(--accent); color: #fff; }
.form-group { margin-bottom: 1rem; }
.form-group label { display: block; font-size: .8125rem; font-weight: 600; margin-bottom: .25rem; color: #555; }
.form-group input, .form-group select { width: 100%; padding: .5rem .75rem; border: 1px solid var(--border); border-radius: 8px; font-size: .875rem; }
.grid-3 { display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 1rem; }
.grid-4 { display: grid; grid-template-columns: 1fr 1fr 1fr 1fr; gap: 1rem; }
.text-sm { font-size: .8125rem; color: #777; }
.badge { display: inline-block; padding: .125rem .5rem; border-radius: 1rem; font-size: .75rem; font-weight: 600; }
.badge-success { background: #55efc4; color: #006266; }
.badge-warning { background: #ffeaa7; color: #8b6914; }
.badge-info { background: #74b9ff; color: #1e3799; }
.badge-danger { background: #fab1a0; color: #c0392b; }
</style>
