<template>
  <div class="app-shell">
    <header class="app-header">
      <h1>🩺 醫生工作台</h1>
      <nav>
        <router-link to="/">排班</router-link>
        <router-link to="/queue">就診隊列</router-link>
        <router-link to="/patients">患者管理</router-link>
        <router-link to="/prescriptions">處方記錄</router-link>
      </nav>
    </header>
    <main><router-view /></main>
  </div>
</template>

<style>
:root { --primary: #2d6a4f; --accent: #d4a373; --bg: #f8faf8; }
* { box-sizing: border-box; margin: 0; padding: 0; }
body { font-family: 'Noto Sans HK', sans-serif; background: var(--bg); }
.app-header { background: var(--primary); color: #fff; padding: .75rem 1.5rem; display: flex; align-items: center; gap: 2rem; }
.app-header h1 { font-size: 1.1rem; }
.app-header nav { display: flex; gap: 1rem; }
.app-header nav a { color: rgba(255,255,255,.8); text-decoration: none; font-size: .875rem; }
.app-header nav a.router-link-active { color: #fff; font-weight: 600; }
main { max-width: 1000px; margin: 0 auto; padding: 1.5rem; min-height: calc(100vh - 60px); }
.card { background: #fff; border-radius: 12px; padding: 1.25rem; margin-bottom: 1rem; box-shadow: 0 1px 4px rgba(0,0,0,.06); }
.btn { padding: .5rem 1rem; border: none; border-radius: 8px; font-size: .8125rem; cursor: pointer; }
.btn-primary { background: var(--primary); color: #fff; }
.btn-danger { background: #e74c3c; color: #fff; }
.form-group { margin-bottom: .75rem; }
.form-group label { display: block; font-size: .75rem; font-weight: 600; margin-bottom: .25rem; color: #555; }
.form-group input, .form-group textarea, .form-group select { width: 100%; padding: .5rem; border: 1px solid #ddd; border-radius: 6px; font-size: .8125rem; }
.form-group textarea { min-height: 60px; }
.flex { display: flex; gap: .75rem; align-items: center; }
.grid-2 { display: grid; grid-template-columns: 1fr 1fr; gap: .75rem; }
.grid-3 { display: grid; grid-template-columns: 1fr 1fr 1fr; gap: .75rem; }
</style>
