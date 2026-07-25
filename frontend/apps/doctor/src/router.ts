import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@utils/stores/auth'

const routes = [
  { path: '/login', name: 'Login', component: () => import('./views/LoginView.vue') },
  { path: '/', name: 'PatientList', component: () => import('./views/PatientListView.vue'), meta: { requiresAuth: true } },
  { path: '/emr/new/:patientId', name: 'EmrCreate', component: () => import('./views/EmrCreateView.vue'), meta: { requiresAuth: true } },
  { path: '/prescription/new/:patientId', name: 'PrescriptionCreate', component: () => import('./views/PrescriptionCreateView.vue'), meta: { requiresAuth: true } },
]

const router = createRouter({ history: createWebHistory(), routes })

router.beforeEach((to, _from, next) => {
  if (to.meta.requiresAuth && !useAuthStore().token) next('/login')
  else next()
})

export default router
