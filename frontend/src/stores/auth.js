import { defineStore } from 'pinia'
import { api, setSession, clearSession, currentUser } from '../api/client'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('cm_token') || '',
    user: currentUser()
  }),
  getters: {
    isLogin: (s) => !!s.token,
    role: (s) => s.user?.role || '',
    isAdmin: (s) => s.user?.role === 'ADMIN',
    isStaff: (s) => s.user?.role === 'STAFF',
    isVendor: (s) => s.user?.role === 'VENDOR'
  },
  actions: {
    async login(username, password) {
      const data = await api.post('/auth/login', { username, password })
      this.token = data.token
      this.user = data.user
      setSession(data.token, data.user)
      return data.user
    },
    logout() {
      this.token = ''
      this.user = null
      clearSession()
    }
  }
})
