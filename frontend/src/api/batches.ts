import http from './http'
import type {
  BatchDiagnosis,
  BatchStatus,
  MlPredictionLog,
  PageResponse,
  ProcessType,
  QcMeasurement,
  RamanSpectrumRaw,
  SynthesisBatch,
  SynthesisProcessParam,
} from '../types'

export interface BatchListParams {
  page?: number
  size?: number
  sort?: string[]
  batchNo?: string
  status?: BatchStatus
  startDate?: string
  endDate?: string
  orgId?: number
}

export function listBatches(params: BatchListParams = {}) {
  return http.get<PageResponse<SynthesisBatch>>('/batches', { params })
}

export function getBatch(batchId: number) {
  return http.get<SynthesisBatch>(`/batches/${batchId}`)
}

export interface BatchCreatePayload {
  batchNo: string
  equipmentModelId: number
  substrateId: number
  rawMaterialId?: number
  processType: ProcessType
  startedAt?: string
  recipeVersion?: string
  notes?: string
}

export function createBatch(payload: BatchCreatePayload) {
  return http.post<SynthesisBatch>('/batches', payload)
}

export function updateBatchStatus(batchId: number, status: BatchStatus) {
  return http.patch<SynthesisBatch>(`/batches/${batchId}/status`, { status })
}

export function listProcessParams(batchId: number, paramName?: string) {
  return http.get<SynthesisProcessParam[]>(`/batches/${batchId}/process-params`, { params: { paramName } })
}

export interface ProcessParamInput {
  paramName: string
  paramValue: number
  unit: string
  recordedAt?: string
}

export function createProcessParams(batchId: number, params: ProcessParamInput[]) {
  return http.post<SynthesisProcessParam[]>(`/batches/${batchId}/process-params`, { params })
}

export function listQcMeasurements(batchId: number) {
  return http.get<QcMeasurement[]>(`/batches/${batchId}/qc-measurements`)
}

export interface QcMeasurementInput {
  metricCode: string
  measuredValue?: number
  unit: string
  measurementEquipmentId?: number
  measuredAt: string
  notes?: string
}

export function createQcMeasurement(batchId: number, payload: QcMeasurementInput) {
  return http.post<QcMeasurement>(`/batches/${batchId}/qc-measurements`, payload)
}

export function listRamanSpectra(batchId: number) {
  return http.get<RamanSpectrumRaw[]>(`/batches/${batchId}/raman-spectra`)
}

export function uploadRamanSpectrum(batchId: number, file: File, measurementEquipmentId?: number, measuredAt?: string) {
  const form = new FormData()
  form.append('file', file)
  if (measurementEquipmentId) form.append('measurementEquipmentId', String(measurementEquipmentId))
  if (measuredAt) form.append('measuredAt', measuredAt)
  return http.post<RamanSpectrumRaw>(`/batches/${batchId}/raman-spectra`, form, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

export function getBatchDiagnosis(batchId: number) {
  return http.get<BatchDiagnosis>(`/batches/${batchId}/diagnosis`)
}

export function listBatchMlPredictions(batchId: number) {
  return http.get<MlPredictionLog[]>(`/batches/${batchId}/ml-predictions`)
}

export function requestMlPrediction(batchId: number, targetMetricCode: string) {
  return http.post<MlPredictionLog>(`/batches/${batchId}/ml-predictions`, { targetMetricCode })
}

export function getMlPredictionDetail(batchId: number, predictionId: number) {
  return http.get<MlPredictionLog>(`/batches/${batchId}/ml-predictions/${predictionId}`)
}
