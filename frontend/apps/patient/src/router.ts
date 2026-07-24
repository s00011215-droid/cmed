import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@utils/stores/auth'

const routes = [
  { path: '/login', name: 'Login', component: () => import('./views/LoginView.vue') },
  { path: '/', name: 'Home', component: () => import('./views/HomeView.vue'), meta: { requiresAuth: true } },
  { path: '/patients/:id', name: 'PatientDetail', component: () => import('./views/PatientDetailView.vue'), meta: { requiresAuth: true } },
  { path: '/consultations', name: 'Consultations', component: () => import('./views/ConsultationsView.vue'), meta: { requiresAuth: true } },
  { path: '/prescriptions', name: 'Prescriptions', component: () => import('./views/PrescriptionsView.vue'), meta: { requiresAuth: true } },
  { path: '/profile', name: 'Profile', component: () => import('./views/ProfileView.vue'), meta: { requiresAuth: true } },
]

const router = createRouter({ history: createWebHistory(), routes })

// Auth guard
router.beforeEach((to, _from, next) => {
  if (to.meta.requiresAuth && !useAuthStore().token) next('/login')
  else next()
})

export default router
