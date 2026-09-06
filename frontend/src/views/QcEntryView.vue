<!-- SCR-06: 품질측정 데이터 등록 -->
<template>
  <div class="mx-auto max-w-3xl space-y-6">
    <div class="surface-card rounded-lg p-6">
      <h2 class="mb-4 text-base font-semibold">대상 배치 선택</h2>
      <select v-model.number="selectedBatchId" class="input" @change="onBatchChange">
        <option value="" disabled>배치를 선택하세요</option>
        <option v-for="b in batches" :key="b.batchId" :value="b.batchId">
          {{ b.batchNo }} ({{ b.status }})
        </option>
      </select>
    </div>

    <template v-if="selectedBatchId">
      <!-- 라만 스펙트럼 업로드 -->
      <div class="surface-card rounded-lg p-6">
        <h2 class="mb-4 text-base font-semibold">라만 스펙트럼 CSV 업로드</h2>
        <div
          class="flex flex-col items-center justify-center gap-2 rounded-lg border-2 border-dashed border-slate-300 p-8 text-center dark:border-slate-600"
          @dragover.prevent
          @drop.prevent="onDrop"
        >
          <UploadCloud class="h-8 w-8 text-slate-400" />
          <p class="text-sm text-slate-500 dark:text-slate-400">CSV 파일을 여기에 드래그하거나</p>
          <label class="btn btn-secondary cursor-pointer">
            파일 선택
            <input type="file" accept=".csv" class="hidden" @change="onFileSelect" />
          </label>
          <p v-if="selectedFile" class="text-xs text-cyan-600 dark:text-cyan-400">{{ selectedFile.name }}</p>
        </div>
        <div v-if="csvPreview.length" class="mt-4">
          <p class="mb-1 text-xs font-medium text-slate-500">미리보기 (상위 5행)</p>
          <table class="w-full text-xs">
            <thead>
              <tr class="border-b border-slate-200 dark:border-slate-700/60">
                <th class="py-1 text-left">wavenumber (cm⁻¹)</th>
                <th class="py-1 text-left">intensity</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(row, i) in csvPreview" :key="i">
                <td class="py-0.5 tabular-nums">{{ row[0] }}</td>
                <td class="py-0.5 tabular-nums">{{ row[1] }}</td>
              </tr>
            </tbody>
          </table>
        </div>
        <div class="mt-4 flex justify-end">
          <button class="btn btn-primary" :disabled="!selectedFile || uploadingSpectrum" @click="uploadSpectrum">
            <Loader2 v-if="uploadingSpectrum" class="h-4 w-4 animate-spin" />
            업로드
          </button>
        </div>
      </div>

      <!-- 물성치 직접입력 -->
      <div class="surface-card rounded-lg p-6">
        <h2 class="mb-4 text-base font-semibold">물성치 직접 입력</h2>
        <form class="grid grid-cols-2 gap-4" @submit.prevent="submitMeasurement">
          <div>
            <label class="label">측정 지표 (metric_code) *</label>
            <input v-model="measurementForm.metricCode" required class="input" placeholder="예: ID_IG_RATIO" />
          </div>
          <div>
            <label class="label">측정값 *</label>
            <input v-model.number="measurementForm.measuredValue" type="number" step="any" required class="input" />
          </div>
          <div>
            <label class="label">단위 *</label>
            <input v-model="measurementForm.unit" required class="input" placeholder="예: ratio, %, S/m" />
          </div>
          <div>
            <label class="label">측정장비</label>
            <select v-model.number="measurementForm.measurementEquipmentId" class="input">
              <option :value="undefined">선택 안함</option>
              <option v-for="eq in measurementEquipments" :key="eq.measurementEquipmentId" :value="eq.measurementEquipmentId">
                {{ eq.equipmentName }}
              </option>
            </select>
          </div>
          <div>
            <label class="label">측정일시 *</label>
            <input v-model="measurementForm.measuredAt" type="datetime-local" required class="input" />
          </div>
          <div class="col-span-2">
            <label class="label">비고</label>
            <input v-model="measurementForm.notes" class="input" />
          </div>

          <div v-if="judgedPreview" class="col-span-2 flex items-center gap-2 rounded-md bg-slate-50 px-3 py-2 dark:bg-elevation-2">
            <span class="text-xs text-slate-500">자동판정 결과:</span>
            <StatusBadge :status="judgedPreview.judgedResult || 'PASS'" />
            <span v-if="judgedPreview.judgedSpecType" class="text-xs text-slate-400">
              (적용기준: {{ judgedPreview.judgedSpecType }})
            </span>
          </div>

          <div class="col-span-2 flex justify-end">
            <button type="submit" :disabled="savingMeasurement" class="btn btn-primary">
              <Loader2 v-if="savingMeasurement" class="h-4 w-4 animate-spin" />
              저장
            </button>
          </div>
        </form>
      </div>

      <!-- 저장된 측정값 목록 -->
      <div v-if="measurements.length" class="surface-card rounded-lg p-4">
        <h3 class="mb-3 text-sm font-semibold">등록된 측정값</h3>
        <table class="w-full text-sm">
          <thead>
            <tr class="border-b border-slate-200 text-left text-xs text-slate-400 dark:border-slate-700/60">
              <th class="pb-2">지표</th>
              <th class="pb-2">값</th>
              <th class="pb-2">판정</th>
              <th class="pb-2">측정일시</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="m in measurements" :key="m.qcMeasurementId" class="border-b border-slate-100 dark:border-slate-800">
              <td class="py-1.5">{{ m.metricCode }}</td>
              <td class="py-1.5 tabular-nums">{{ m.measuredValue }} {{ m.unit }}</td>
              <td class="py-1.5"><StatusBadge v-if="m.judgedResult" :status="m.judgedResult" /></td>
              <td class="py-1.5 text-xs text-slate-500">{{ formatDate(m.measuredAt) }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { UploadCloud, Loader2 } from 'lucide-vue-next'
import StatusBadge from '../components/StatusBadge.vue'
import { listBatches } from '../api/batches'
import { listMeasurementEquipments } from '../api/masterdata'
import { createQcMeasurement, listQcMeasurements, uploadRamanSpectrum } from '../api/batches'
import type { MeasurementEquipment, QcMeasurement, SynthesisBatch } from '../types'

const batches = ref<SynthesisBatch[]>([])
const measurementEquipments = ref<MeasurementEquipment[]>([])
const selectedBatchId = ref<number | ''>('')
const measurements = ref<QcMeasurement[]>([])

const selectedFile = ref<File | null>(null)
const csvPreview = ref<string[][]>([])
const uploadingSpectrum = ref(false)

const measurementForm = ref({
  metricCode: '',
  measuredValue: undefined as number | undefined,
  unit: '',
  measurementEquipmentId: undefined as number | undefined,
  measuredAt: new Date().toISOString().slice(0, 16),
  notes: '',
})
const savingMeasurement = ref(false)
const judgedPreview = ref<QcMeasurement | null>(null)

async function loadInitial() {
  const [b, eq] = await Promise.all([listBatches({ size: 100, status: 'COMPLETED' }), listMeasurementEquipments({ size: 100 })])
  batches.value = b.data.content
  measurementEquipments.value = eq.data.content
}

async function onBatchChange() {
  if (!selectedBatchId.value) return
  const res = await listQcMeasurements(Number(selectedBatchId.value))
  measurements.value = res.data
}

function parseCsvPreview(text: string) {
  const lines = text.trim().split('\n').slice(0, 5)
  csvPreview.value = lines.map((l) => l.split(',').map((c) => c.trim()))
}

function onFileSelect(e: Event) {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (!file) return
  selectedFile.value = file
  file.text().then(parseCsvPreview)
}

function onDrop(e: DragEvent) {
  const file = e.dataTransfer?.files?.[0]
  if (!file) return
  selectedFile.value = file
  file.text().then(parseCsvPreview)
}

async function uploadSpectrum() {
  if (!selectedFile.value || !selectedBatchId.value) return
  uploadingSpectrum.value = true
  try {
    await uploadRamanSpectrum(Number(selectedBatchId.value), selectedFile.value)
    selectedFile.value = null
    csvPreview.value = []
  } finally {
    uploadingSpectrum.value = false
  }
}

async function submitMeasurement() {
  if (!selectedBatchId.value) return
  savingMeasurement.value = true
  try {
    const res = await createQcMeasurement(Number(selectedBatchId.value), {
      metricCode: measurementForm.value.metricCode,
      measuredValue: measurementForm.value.measuredValue,
      unit: measurementForm.value.unit,
      measurementEquipmentId: measurementForm.value.measurementEquipmentId,
      measuredAt: new Date(measurementForm.value.measuredAt).toISOString(),
      notes: measurementForm.value.notes || undefined,
    })
    judgedPreview.value = res.data
    await onBatchChange()
    measurementForm.value.metricCode = ''
    measurementForm.value.measuredValue = undefined
  } finally {
    savingMeasurement.value = false
  }
}

function formatDate(d: string) {
  return new Date(d).toLocaleString('ko-KR')
}

loadInitial()
</script>
