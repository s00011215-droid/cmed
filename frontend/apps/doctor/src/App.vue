<script setup lang="ts">
import { RouterView } from 'vue-router'
</script>

<template>
  <div class="app-shell">
    <header class="app-header">
      <h1>🩺 祥雲智方 · 醫生工作台</h1>
      <nav>
        <router-link to="/">患者列表</router-link>
      </nav>
    </header>
    <main><router-view /></main>
    <footer class="app-footer">© 2026 祥雲智方中醫診所</footer>
  </div>
</template>

<style>
:root { --primary: #2d6a4f; --accent: #d4a373; --bg: #f8faf8; --card: #fff; --text: #333; --border: #e0e0e0; }
* { box-sizing: border-box; margin: 0; padding: 0; }
body { font-family: 'Noto Sans HK', -apple-system, sans-serif; background: var(--bg); color: var(--text); }
.app-header { background: var(--primary); color: #fff; padding: 1rem 1.5rem; display: flex; align-items: center; justify-content: space-between; }
.app-header h1 { font-size: 1.25rem; }
.app-header nav { display: flex; gap: 1rem; }
.app-header nav a { color: rgba(255,255,255,.85); text-decoration: none; font-size: .875rem; }
.app-header nav a.router-link-active { color: #fff; font-weight: 600; }
main { max-width: 900px; margin: 0 auto; padding: 1.5rem 1rem; min-height: calc(100vh - 120px); }
.app-footer { text-align: center; padding: 1rem; font-size: .75rem; color: #999; border-top: 1px solid var(--border); }
.card { background: var(--card); border-radius: 12px; padding: 1.25rem; margin-bottom: 1rem; box-shadow: 0 1px 4px rgba(0,0,0,.06); }
.btn { display: inline-flex; align-items: center; gap: .5rem; padding: .625rem 1.25rem; border: none; border-radius: 8px; font-size: .875rem; font-weight: 500; cursor: pointer; transition: all .2s; }
.btn-primary { background: var(--primary); color: #fff; }
.btn-accent { background: var(--accent); color: #fff; }
.btn-outline { background: transparent; border: 1px solid var(--primary); color: var(--primary); }
.form-group { margin-bottom: 1rem; }
.form-group label { display: block; font-size: .8125rem; font-weight: 600; margin-bottom: .25rem; color: #555; }
.form-group input, .form-group textarea, .form-group select { width: 100%; padding: .5rem .75rem; border: 1px solid var(--border); border-radius: 8px; font-size: .875rem; }
.form-group textarea { min-height: 80px; resize: vertical; }
.flex { display: flex; gap: .75rem; align-items: center; }
.grid-2 { display: grid; grid-template-columns: 1fr 1fr; gap: .75rem; }
.grid-3 { display: grid; grid-template-columns: 1fr 1fr 1fr; gap: .75rem; }
.text-sm { font-size: .8125rem; color: #777; }
.badge { display: inline-block; padding: .125rem .5rem; border-radius: 1rem; font-size: .75rem; font-weight: 600; }
.badge-online { background: #55efc4; color: #006266; }
.badge-offline { background: #74b9ff; color: #1e3799; }
.badge-draft { background: #ffeaa7; color: #8b6914; }
.badge-paid { background: #74b9ff; color: #1e3799; }
</style>
