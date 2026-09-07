import axios from 'axios'
import router from '../router'

// VITE_API_BASE_URL이 명시적으로 설정되지 않은 경우, 현재 브라우저가 접속한 hostname을 그대로 써서
// 백엔드(8089)를 호출한다. localhost로 접속하든 VM의 공개 IP/도메인으로 접속하든 동일하게 동작하게 함.
// (2026-09-07 수정: VITE_API_BASE_URL=http://localhost:8089 하드코딩으로 인해 외부(공개IP 등)에서
//  접속 시 브라우저가 '자기 자신의 localhost:8089'를 호출하려다 실패하여 로그인이 안 되던 버그 수정)
const baseURL = import.meta.env.VITE_API_BASE_URL || `http://${window.location.hostname}:8089/api/v1`

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
