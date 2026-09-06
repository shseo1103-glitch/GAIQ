import { defineStore } from 'pinia'
import { login as loginApi, fetchMe } from '../api/auth'
import type { UserAccount } from '../types'

const TOKEN_KEY = 'gaiq-access-token'
const REFRESH_KEY = 'gaiq-refresh-token'
const USER_KEY = 'gaiq-user'

function loadStoredUser(): UserAccount | null {
  const raw = localStorage.getItem(USER_KEY)
  if (!raw) return null
  try {
    return JSON.parse(raw) as UserAccount
  } catch {
    return null
  }
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    accessToken: localStorage.getItem(TOKEN_KEY) || '',
    refreshToken: localStorage.getItem(REFRESH_KEY) || '',
    user: loadStoredUser(),
    /** PLATFORM_ADMIN이 전체조직 합산 뷰를 볼지 토글하는 UI 상태 (대시보드 SCR-02) */
    allOrgsView: false,
  }),
  getters: {
    isAuthenticated: (state) => !!state.accessToken,
    isPlatformAdmin: (state) => state.user?.role === 'PLATFORM_ADMIN',
    role: (state) => state.user?.role,
  },
  actions: {
    async login(orgCode: string, loginId: string, password: string) {
      const res = await loginApi(orgCode, loginId, password)
      const data = res.data
      this.accessToken = data.accessToken
      this.refreshToken = data.refreshToken
      this.user = data.user
      localStorage.setItem(TOKEN_KEY, data.accessToken)
      localStorage.setItem(REFRESH_KEY, data.refreshToken)
      localStorage.setItem(USER_KEY, JSON.stringify(data.user))
      return data
    },
    async loadMe() {
      if (!this.accessToken) return null
      const res = await fetchMe()
      this.user = res.data
      localStorage.setItem(USER_KEY, JSON.stringify(res.data))
      return this.user
    },
    logout() {
      this.accessToken = ''
      this.refreshToken = ''
      this.user = null
      localStorage.removeItem(TOKEN_KEY)
      localStorage.removeItem(REFRESH_KEY)
      localStorage.removeItem(USER_KEY)
    },
    toggleAllOrgsView() {
      this.allOrgsView = !this.allOrgsView
    },
  },
})
