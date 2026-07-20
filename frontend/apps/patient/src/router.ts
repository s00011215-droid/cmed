import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/login', name: 'Login', component: () => import('../views/LoginView.vue') },
  { path: '/', name: 'Home', component: () => import('../views/HomeView.vue'), meta: { requiresAuth: true } },
  { path: '/patients/:id', name: 'PatientDetail', component: () => import('../views/PatientDetailView.vue'), meta: { requiresAuth: true } },
]

import { useAuthStore } from '@utils/stores/auth'

export default createRouter({
  history: createWebHistory(),
  routes,
})
