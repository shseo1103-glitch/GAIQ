// GAIQ 1단계(MVP) 타입 정의 — docs/api-contract/01-gaiq-core.yaml 발췌

export type OrgType = 'PRODUCER_GRAPHENE_ELECTRIC' | 'ADOPTER' | 'ADMIN'
export type OrgStatus = 'PENDING' | 'ACTIVE' | 'SUSPENDED'
export type UserRole = 'PROCESS_ENGINEER' | 'OPERATOR' | 'PLATFORM_ADMIN'
export type UserStatus = 'ACTIVE' | 'SUSPENDED'
export type ProcessType = 'BATCH' | 'R2R'
export type BatchStatus = 'PLANNED' | 'RUNNING' | 'COMPLETED' | 'FAILED' | 'ABORTED'
export type JudgedResult = 'PASS' | 'FAIL'
export type SpecType = 'TARGET_STAGE1' | 'TARGET_STAGE2' | 'VALIDATED_ACHIEVEMENT'
export type Comparator = '<=' | '>=' | '=' | 'RANGE'
export type ConfidenceLevel = 'HIGH' | 'MEDIUM' | 'LOW' | 'DATA_INSUFFICIENT'
export type Algorithm = 'GPR' | 'RANDOM_FOREST' | 'LINEAR_REGRESSION' | 'RULE_BASED_FALLBACK'
export type ParamName =
  | 'CHAMBER_TEMP_C'
  | 'CH4_FLOW_SCCM'
  | 'H2_FLOW_SCCM'
  | 'AR_FLOW_SCCM'
  | 'CHAMBER_PRESSURE_PA'
  | 'ANNEAL_TIME_MIN'
  | 'GROWTH_TIME_MIN'
  | 'ROLL_TENSION_KGM'
  | 'WINDING_SPEED_MH'
  | 'OTHER'
export type ActionType = 'CREATE' | 'UPDATE' | 'DELETE' | 'LOGIN' | 'LOGOUT'

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  page: number
  size: number
}

export interface Organization {
  orgId: number
  orgCode: string
  orgType: OrgType
  orgName: string
  industryType?: string | null
  businessRegNo?: string | null
  status: OrgStatus
  contactName?: string | null
  contactEmail?: string | null
  contactPhone?: string | null
  approvedBy?: number | null
  approvedAt?: string | null
  createdAt?: string
  updatedAt?: string
}

export interface UserAccount {
  userId: number
  orgId: number
  loginId: string
  role: UserRole
  name: string
  email?: string | null
  phone?: string | null
  status: UserStatus
  lastLoginAt?: string | null
  createdAt?: string
  updatedAt?: string
}

export interface EquipmentModel {
  equipmentModelId: number
  modelCode: string
  modelName: string
  equipmentType: string
  processType: ProcessType
  manufacturer?: string | null
}

export interface RawMaterial {
  rawMaterialId: number
  materialCode: string
  materialName: string
  materialType: string
  purityPct?: number | null
  lotNo?: string | null
  supplier?: string | null
}

export interface Substrate {
  substrateId: number
  substrateCode: string
  substrateType: string
  specDesignation?: string | null
  crossSectionMm2?: number | null
  diameterMm?: number | null
  strandCount?: number | null
}

export interface MeasurementEquipment {
  measurementEquipmentId: number
  equipmentCode: string
  equipmentName: string
  equipmentType: string
  manufacturer?: string | null
  calibrationDueDate?: string | null
}

export interface QcThresholdSpec {
  thresholdId: number
  metricCode: string
  metricName: string
  unit: string
  specType: SpecType
  comparator: Comparator
  thresholdValue?: number | null
  thresholdValueMax?: number | null
  sourceDoc?: string | null
  orgId?: number | null
  effectiveFrom?: string | null
}

export interface SynthesisBatch {
  batchId: number
  orgId: number
  batchNo: string
  equipmentModelId: number
  substrateId: number
  rawMaterialId?: number | null
  processType: ProcessType
  startedAt?: string | null
  endedAt?: string | null
  status: BatchStatus
  operatorUserId?: number | null
  recipeVersion?: string | null
  notes?: string | null
  createdAt?: string
  updatedAt?: string
}

export interface SynthesisProcessParam {
  paramId: number
  batchId: number
  paramName: ParamName
  paramValue: number
  unit: string
  recordedAt: string
}

export interface QcMeasurement {
  qcMeasurementId: number
  batchId: number
  metricCode: string
  measuredValue?: number | null
  unit: string
  measurementEquipmentId?: number | null
  measuredAt: string
  judgedResult?: JudgedResult | null
  judgedSpecType?: SpecType | null
  measuredByUserId?: number | null
  notes?: string | null
}

export interface RamanSpectrumRaw {
  spectrumId: number
  batchId: number
  measurementEquipmentId?: number | null
  scanRangeCm1?: string | null
  wavenumberArray: number[]
  intensityArray: number[]
  rawFilePath?: string | null
  measuredAt: string
}

export interface BatchDiagnosisMetricResult {
  metricCode: string
  measuredValue?: number | null
  judgedResult: JudgedResult
  appliedSpecType: SpecType
}

export interface BatchDiagnosisImprovement {
  paramName: string
  currentValue?: number | null
  recommendedValue?: number | null
  rationale: string
}

export interface BatchDiagnosis {
  batchId: number
  grade: 'A' | 'B' | 'C'
  metricResults: BatchDiagnosisMetricResult[]
  rootCauseSummary: string
  improvementSuggestions: BatchDiagnosisImprovement[]
}

export interface MlModelVersion {
  modelId: number
  targetMetricCode: string
  algorithm: Algorithm
  versionNo: string
  trainedAt: string
  trainingDataCount: number
  r2Score?: number | null
  mae?: number | null
  rmse?: number | null
  kernelType?: string | null
  modelFilePath: string
  isActive: boolean
}

export interface MlPredictionLog {
  predictionId: number
  batchId: number
  modelId: number
  inputParamsJson?: Record<string, unknown>
  predictedValue: number
  predictedStdDev?: number | null
  confidenceLevel?: ConfidenceLevel | null
  actualValue?: number | null
  residual?: number | null
  predictedAt: string
}

export interface ActionLog {
  actionLogId: number
  orgId?: number | null
  actorUserId?: number | null
  actorType: 'USER' | 'SYSTEM'
  module: string
  actionType: ActionType
  targetEntity: string
  targetEntityId?: number | null
  beforeData?: Record<string, unknown> | null
  afterData?: Record<string, unknown> | null
  clientIp?: string | null
  occurredAt: string
}

export interface LoginResponse {
  accessToken: string
  refreshToken: string
  user: UserAccount
}
