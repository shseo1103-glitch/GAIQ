import http from './http'
import type { EquipmentModel, MeasurementEquipment, PageResponse, QcThresholdSpec, RawMaterial, Substrate } from '../types'

export function listEquipmentModels(params: { page?: number; size?: number; equipmentType?: string } = {}) {
  return http.get<PageResponse<EquipmentModel>>('/equipment-models', { params })
}

export function listRawMaterials(params: { page?: number; size?: number } = {}) {
  return http.get<PageResponse<RawMaterial>>('/raw-materials', { params })
}

export function listSubstrates(params: { page?: number; size?: number; substrateType?: string } = {}) {
  return http.get<PageResponse<Substrate>>('/substrates', { params })
}

export function listMeasurementEquipments(params: { page?: number; size?: number } = {}) {
  return http.get<PageResponse<MeasurementEquipment>>('/measurement-equipments', { params })
}

export function listQcThresholdSpecs(params: { metricCode?: string; specType?: string; orgId?: number } = {}) {
  return http.get<QcThresholdSpec[]>('/qc-threshold-specs', { params })
}
