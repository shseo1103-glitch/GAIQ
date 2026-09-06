import axios from 'axios'
import router from '../router'

const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8089/api/v1'

const http = axios.create({
  baseURL,
  headers: {
    'Content-Type': 'application/json',
  },
})

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('gaiq-access-token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('gaiq-access-token')
      localStorage.removeItem('gaiq-refresh-token')
      localStorage.removeItem('gaiq-user')
      if (router.currentRoute.value.name !== 'login') {
        router.push({ name: 'login' })
      }
    }
    return Promise.reject(error)
  },
)

export default http
export { baseURL }
