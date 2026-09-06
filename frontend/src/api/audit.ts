import http from './http'
import type { ActionLog, PageResponse } from '../types'

export interface ActionLogListParams {
  page?: number
  size?: number
  sort?: string[]
  module?: string
  actorUserId?: number
  startDate?: string
  endDate?: string
}

export function listActionLogs(params: ActionLogListParams = {}) {
  return http.get<PageResponse<ActionLog>>('/audit/action-logs', { params })
}

export function getActionLog(actionLogId: number) {
  return http.get<ActionLog>(`/audit/action-logs/${actionLogId}`)
}
