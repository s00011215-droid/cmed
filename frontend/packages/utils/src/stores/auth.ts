import { defineStore } from 'pinia'

interface AuthState {
  token: string | null
  refreshToken: string | null
  user: { userId: number; username: string; realName: string; role: string; clinicId: number } | null
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    token: localStorage.getItem('token'),
    refreshToken: localStorage.getItem('refreshToken'),
    user: JSON.parse(localStorage.getItem('user') || 'null'),
  }),
  getters: {
    isLoggedIn: (s) => !!s.token,
    role: (s) => s.user?.role,
  },
  actions: {
    setAuth(token: string, refreshToken: string, user: AuthState['user']) {
      this.token = token; this.refreshToken = refreshToken; this.user = user
      localStorage.setItem('token', token)
      localStorage.setItem('refreshToken', refreshToken)
      localStorage.setItem('user', JSON.stringify(user))
    },
    logout() {
      this.token = null; this.refreshToken = null; this.user = null
      localStorage.clear()
    }
  }
})
