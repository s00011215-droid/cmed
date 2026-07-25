import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@utils/stores/auth'

const modules = ['account','consult','prescription','his','inventory','finance','decoction','logistics','insurance','notify','risk','admin']

const routes = [
  { path: '/login', name: 'Login', component: () => import('./views/LoginView.vue') },
  { path: '/', name: 'Dashboard', component: () => import('./views/DashboardView.vue'), meta: { requiresAuth: true } },
  ...modules.map(m => ({ path: `/${m}`, name: m, component: () => import('./views/ModuleView.vue'), meta: { requiresAuth: true, module: m } })),
]

const router = createRouter({ history: createWebHistory(), routes })

router.beforeEach((to, _from, next) => {
  if (to.meta.requiresAuth && !useAuthStore().token) next('/login')
  else next()
})

export default router
