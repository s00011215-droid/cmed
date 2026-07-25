import axios from 'axios'
import { useAuthStore } from '../stores/auth'
import type { Result } from '../types'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || '',
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' }
})

// 請求攔截器 — 自動附加 JWT
api.interceptors.request.use(config => {
  const token = useAuthStore().token
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

// 回應攔截器 — 統一錯誤處理
api.interceptors.response.use(
  res => res,
  err => {
    if (err.response?.status === 401) {
      useAuthStore().logout()
      window.location.href = '/login'
    }
    return Promise.reject(err)
  }
)

// --- API 方法 ---
export const accountApi = {
  login: (data: { username: string; password: string }) => api.post<Result<any>>('/api/v1/account/login', data),
}

export const patientApi = {
  search: (keyword: string, page = 1, size = 20) => api.get<Result<any>>('/api/v1/patient', { params: { keyword, page, size } }),
  get: (id: number) => api.get<Result<any>>(`/api/v1/patient/${id}`),
  save: (data: any) => api.post<Result<number>>('/api/v1/patient', data),
}

export const emrApi = {
  listByPatient: (patientId: number, page = 1) => api.get<Result<any>>(`/api/v1/emr/patient/${patientId}`, { params: { page } }),
  get: (id: number) => api.get<Result<any>>(`/api/v1/emr/${id}`),
  save: (data: any) => api.post<Result<number>>('/api/v1/emr', data),
  search: (keyword: string) => api.get<Result<any>>('/api/v1/emr/search', { params: { keyword } }),
}

export const prescriptionApi = {
  listByPatient: (patientId: number) => api.get<Result<any>>(`/api/v1/prescription/patient/${patientId}`),
  get: (id: number) => api.get<Result<any>>(`/api/v1/prescription/${id}`),
  save: (data: any) => api.post<Result<any>>('/api/v1/prescription', data),
  transition: (id: number, status: string) => api.post<Result<void>>(`/api/v1/prescription/${id}/transition`, { status }),
}

export const decoctionApi = {
  create: (data: any) => api.post<Result<any>>('/api/v1/decoction/orders', data),
  get: (id: number) => api.get<Result<any>>(`/api/v1/decoction/orders/${id}`),
}

export const logisticsApi = {
  getTrace: (waybillNo: string) => api.get<Result<any>>(`/api/v1/logistics/trace/${waybillNo}`),
}

export const adminApi = {
  dashboard: () => api.get<Result<any>>('/api/v1/admin/dashboard'),
}
