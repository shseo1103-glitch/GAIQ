import http from './http'
import type { MlModelVersion, MlPredictionLog, PageResponse } from '../types'

export function listMlModels(params: { targetMetricCode?: string; activeOnly?: boolean } = {}) {
  return http.get<MlModelVersion[]>('/ml-models', { params })
}

export function getMlModel(modelId: number) {
  return http.get<MlModelVersion>(`/ml-models/${modelId}`)
}

export function listMlModelPredictions(modelId: number, params: { page?: number; size?: number } = {}) {
  return http.get<PageResponse<MlPredictionLog>>(`/ml-models/${modelId}/predictions`, { params })
}
