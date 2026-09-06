import http from './http'
import type { Organization, OrgStatus, OrgType, PageResponse, UserAccount } from '../types'

export interface OnboardingRequestPayload {
  orgName: string
  industryType?: string
  businessRegNo?: string
  contactName: string
  contactEmail: string
  contactPhone?: string
}

export function submitOnboardingRequest(payload: OnboardingRequestPayload) {
  return http.post<Organization>('/organizations/onboarding-requests', payload)
}

export function approveOrganization(orgId: number, body?: { initialAdminLoginId?: string; initialAdminEmail?: string }) {
  return http.post<Organization>(`/organizations/${orgId}/approve`, body ?? {})
}

export function rejectOrganization(orgId: number, reason: string) {
  return http.post<Organization>(`/organizations/${orgId}/reject`, { reason })
}

export function listOrganizations(params: { page?: number; size?: number; status?: OrgStatus; orgType?: OrgType } = {}) {
  return http.get<PageResponse<Organization>>('/organizations', { params })
}

export function getOrganization(orgId: number) {
  return http.get<Organization>(`/organizations/${orgId}`)
}

export function updateOrganization(orgId: number, payload: Partial<Organization>) {
  return http.put<Organization>(`/organizations/${orgId}`, payload)
}

export function listOrgUsers(orgId: number, params: { page?: number; size?: number } = {}) {
  return http.get<PageResponse<UserAccount>>(`/organizations/${orgId}/users`, { params })
}

export interface UserCreatePayload {
  loginId: string
  password: string
  role: UserAccount['role']
  name: string
  email?: string
  phone?: string
}

export function createOrgUser(orgId: number, payload: UserCreatePayload) {
  return http.post<UserAccount>(`/organizations/${orgId}/users`, payload)
}

export function updateUser(userId: number, payload: Partial<UserAccount>) {
  return http.put<UserAccount>(`/users/${userId}`, payload)
}

export function resetUserPassword(userId: number) {
  return http.post<{ temporaryPasswordIssued: boolean }>(`/users/${userId}/reset-password`)
}
