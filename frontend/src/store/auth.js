import { reactive, computed } from 'vue'

const STORAGE_KEY = 'cm_auth'

const saved = JSON.parse(localStorage.getItem(STORAGE_KEY) || 'null')

const state = reactive({
  token: saved?.token || '',
  user: saved?.user || null
})

export function useAuth() {
  const isLogin = computed(() => !!state.token)
  const role = computed(() => state.user?.role || '')
  const isAdmin = computed(() => role.value === 'ADMIN')
  const isStaff = computed(() => role.value === 'STAFF' || role.value === 'ADMIN')
  const isVendor = computed(() => role.value === 'VENDOR')

  function setLogin(token, user) {
    state.token = token
    state.user = user
    localStorage.setItem(STORAGE_KEY, JSON.stringify({ token, user }))
  }

  function logout() {
    state.token = ''
    state.user = null
    localStorage.removeItem(STORAGE_KEY)
  }

  return { state, isLogin, role, isAdmin, isStaff, isVendor, setLogin, logout }
}
