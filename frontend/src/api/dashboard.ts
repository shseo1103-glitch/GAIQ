import http from './http'
import type { QcMeasurement } from '../types'

export interface BatchSummary {
  planned: number
  running: number
  completed: number
  failed: number
}

export function getBatchSummary(orgId?: number) {
  return http.get<BatchSummary>('/dashboard/batches/summary', { params: { orgId } })
}

export function getRecentQcResults(limit = 10, orgId?: number) {
  return http.get<QcMeasurement[]>('/dashboard/qc-results/recent', { params: { limit, orgId } })
}

export interface MlConfidenceAlerts {
  lowConfidenceCount: number
  dataInsufficientCount: number
}

export function getMlConfidenceAlerts(orgId?: number) {
  return http.get<MlConfidenceAlerts>('/dashboard/ml-confidence-alerts', { params: { orgId } })
}
