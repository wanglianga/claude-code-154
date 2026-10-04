import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useAuth } from './store/auth.js'
import router from './router.js'

const api = axios.create({ baseURL: '/api', timeout: 15000 })

api.interceptors.request.use(config => {
  const auth = useAuth()
  if (auth.token) config.headers.Authorization = `Bearer ${auth.token}`
  return config
})

api.interceptors.response.use(
  res => res.data,
  err => {
    const data = err.response?.data
    const msg = data?.message || err.message || '请求失败'
    if (err.response?.status === 401) {
      const auth = useAuth()
      auth.logout()
      if (router.currentRoute.value.path !== '/login') {
        router.push('/login')
      }
    }
    ElMessage.error(msg)
    return Promise.reject(new Error(msg))
  }
)

export default api
