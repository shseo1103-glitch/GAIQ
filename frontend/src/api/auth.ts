import http from './http'
import type { LoginResponse, UserAccount } from '../types'

export function login(orgCode: string, loginId: string, password: string) {
  return http.post<LoginResponse>('/auth/login', { orgCode, loginId, password })
}

export function logout() {
  return http.post('/auth/logout')
}

export function fetchMe() {
  return http.get<UserAccount>('/auth/me')
}
