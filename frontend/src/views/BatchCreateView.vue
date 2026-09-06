<!-- SCR-05: 공정 배치 등록 -->
<template>
  <div class="mx-auto max-w-3xl space-y-6">
    <div v-if="createdBatch" class="surface-card flex items-center gap-3 rounded-lg p-4">
      <CheckCircle2 class="h-6 w-6 text-green-600" />
      <div class="flex-1">
        <p class="text-sm font-medium">배치 <b>{{ createdBatch.batchNo }}</b> 가 등록되었습니다.</p>
        <p class="text-xs text-slate-500 dark:text-slate-400">현재 상태: {{ createdBatch.status }}</p>
      </div>
      <button v-if="createdBatch.status === 'PLANNED'" class="btn btn-primary" @click="startRunning">
        <PlayCircle class="h-4 w-4" /> 배치 시작 (RUNNING)
      </button>
      <RouterLink v-else :to="`/batches/${createdBatch.batchId}`" class="btn btn-secondary">상세보기</RouterLink>
    </div>

    <form class="surface-card space-y-6 rounded-lg p-6" @submit.prevent="onSubmit">
      <h2 class="text-base font-semibold">1. 기본 정보</h2>
      <div class="grid grid-cols-2 gap-4">
        <div>
          <label class="label">배치번호 *</label>
          <input v-model="form.batchNo" required class="input" placeholder="예: BATCH-2026-0001" />
        </div>
        <div>
          <label class="label">공정유형</label>
          <div class="flex rounded-md border border-slate-300 p-0.5 dark:border-slate-600">
            <button
              type="button"
              class="flex-1 rounded px-3 py-1.5 text-sm font-medium"
              :class="form.processType === 'BATCH' ? 'bg-cyan-600 text-white' : 'text-slate-500'"
              @click="form.processType = 'BATCH'"
            >
              BATCH
            </button>
            <button
              type="button"
              class="flex-1 rounded px-3 py-1.5 text-sm font-medium"
              :class="form.processType === 'R2R' ? 'bg-cyan-600 text-white' : 'text-slate-500'"
              @click="form.processType = 'R2R'"
            >
              R2R
            </button>
          </div>
        </div>
        <div>
          <label class="label">장비모델 *</label>
          <select v-model.number="form.equipmentModelId" required class="input">
            <option value="" disabled>선택</option>
            <option v-for="e in equipmentModels" :key="e.equipmentModelId" :value="e.equipmentModelId">
              {{ e.modelName }} ({{ e.modelCode }})
            </option>
          </select>
        </div>
        <div>
          <label class="label">기판 *</label>
          <select v-model.number="form.substrateId" required class="input">
            <option value="" disabled>선택</option>
            <option v-for="s in substrates" :key="s.substrateId" :value="s.substrateId">
              {{ s.substrateCode }} <template v-if="s.crossSectionMm2">(SQ {{ s.crossSectionMm2 }}mm²)</template>
            </option>
          </select>
        </div>
        <div>
          <label class="label">원료</label>
          <select v-model.number="form.rawMaterialId" class="input">
            <option :value="undefined">선택 안함</option>
            <option v-for="m in rawMaterials" :key="m.rawMaterialId" :value="m.rawMaterialId">
              {{ m.materialName }} ({{ m.materialCode }})
            </option>
          </select>
        </div>
        <div>
          <label class="label">레시피 버전</label>
          <input v-model="form.recipeVersion" class="input" placeholder="예: v1.2" />
        </div>
      </div>
      <div>
        <label class="label">비고</label>
        <textarea v-model="form.notes" rows="2" class="input" />
      </div>

      <h2 class="border-t border-slate-200 pt-5 text-base font-semibold dark:border-slate-700/60">
        2. 공정 파라미터
      </h2>
      <div class="grid grid-cols-2 gap-x-8 gap-y-5">
        <ParamSliderInput v-model="params.CHAMBER_TEMP_C" label="챔버온도" unit="℃" :min="150" :max="2000" />
        <ParamSliderInput v-model="params.CH4_FLOW_SCCM" label="CH4 유량" unit="sccm" :min="0.15" :max="200" :step="0.05" />
        <ParamSliderInput v-model="params.H2_FLOW_SCCM" label="H2 유량" unit="sccm" :min="0.4" :max="100" :step="0.1" />
        <ParamSliderInput v-model="params.AR_FLOW_SCCM" label="Ar 유량" unit="sccm" :min="10" :max="1000" />
        <ParamSliderInput v-model="params.CHAMBER_PRESSURE_PA" label="챔버압력" unit="Pa" :min="0.6" :max="6.6" :step="0.1" />
        <ParamSliderInput v-model="params.ANNEAL_TIME_MIN" label="어닐링 시간" unit="min" :min="10" :max="240" />
        <ParamSliderInput v-model="params.GROWTH_TIME_MIN" label="성장 시간" unit="min" :min="5" :max="120" />
        <template v-if="form.processType === 'R2R'">
          <ParamSliderInput v-model="params.ROLL_TENSION_KGM" label="롤 장력" unit="kg/m" :min="0.1" :max="5" :step="0.1" />
          <ParamSliderInput v-model="params.WINDING_SPEED_MH" label="권취속도" unit="m/h" :min="2" :max="10" :step="0.1" />
        </template>
      </div>

      <div v-if="errorMessage" class="rounded-md bg-red-100 px-3 py-2 text-xs text-red-700 dark:bg-red-900/30 dark:text-red-300">
        {{ errorMessage }}
      </div>

      <div class="flex justify-end gap-2 border-t border-slate-200 pt-4 dark:border-slate-700/60">
        <button type="submit" :disabled="loading" class="btn btn-primary">
          <Loader2 v-if="loading" class="h-4 w-4 animate-spin" />
          배치 등록
        </button>
      </div>
    </form>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { CheckCircle2, Loader2, PlayCircle } from 'lucide-vue-next'
import ParamSliderInput from '../components/ParamSliderInput.vue'
import { listEquipmentModels, listRawMaterials, listSubstrates } from '../api/masterdata'
import { createBatch, createProcessParams, updateBatchStatus } from '../api/batches'
import type { EquipmentModel, ProcessType, RawMaterial, Substrate, SynthesisBatch } from '../types'

const equipmentModels = ref<EquipmentModel[]>([])
const substrates = ref<Substrate[]>([])
const rawMaterials = ref<RawMaterial[]>([])

const form = reactive({
  batchNo: '',
  processType: 'BATCH' as ProcessType,
  equipmentModelId: '' as unknown as number,
  substrateId: '' as unknown as number,
  rawMaterialId: undefined as number | undefined,
  recipeVersion: '',
  notes: '',
})

const params = reactive({
  CHAMBER_TEMP_C: 800,
  CH4_FLOW_SCCM: 20,
  H2_FLOW_SCCM: 10,
  AR_FLOW_SCCM: 200,
  CHAMBER_PRESSURE_PA: 2.5,
  ANNEAL_TIME_MIN: 60,
  GROWTH_TIME_MIN: 30,
  ROLL_TENSION_KGM: 1,
  WINDING_SPEED_MH: 5,
})

const paramUnits: Record<string, string> = {
  CHAMBER_TEMP_C: '℃',
  CH4_FLOW_SCCM: 'sccm',
  H2_FLOW_SCCM: 'sccm',
  AR_FLOW_SCCM: 'sccm',
  CHAMBER_PRESSURE_PA: 'Pa',
  ANNEAL_TIME_MIN: 'min',
  GROWTH_TIME_MIN: 'min',
  ROLL_TENSION_KGM: 'kg/m',
  WINDING_SPEED_MH: 'm/h',
}

const loading = ref(false)
const errorMessage = ref('')
const createdBatch = ref<SynthesisBatch | null>(null)

async function loadMasterData() {
  const [e, s, m] = await Promise.all([
    listEquipmentModels({ size: 100 }),
    listSubstrates({ size: 100 }),
    listRawMaterials({ size: 100 }),
  ])
  equipmentModels.value = e.data.content
  substrates.value = s.data.content
  rawMaterials.value = m.data.content
}

async function onSubmit() {
  loading.value = true
  errorMessage.value = ''
  try {
    const batchRes = await createBatch({
      batchNo: form.batchNo,
      equipmentModelId: form.equipmentModelId,
      substrateId: form.substrateId,
      rawMaterialId: form.rawMaterialId,
      processType: form.processType,
      recipeVersion: form.recipeVersion || undefined,
      notes: form.notes || undefined,
    })
    const batch = batchRes.data
    const includedKeys =
      form.processType === 'R2R'
        ? Object.keys(params)
        : Object.keys(params).filter((k) => k !== 'ROLL_TENSION_KGM' && k !== 'WINDING_SPEED_MH')

    await createProcessParams(
      batch.batchId,
      includedKeys.map((key) => ({
        paramName: key,
        paramValue: (params as any)[key],
        unit: paramUnits[key],
      })),
    )
    createdBatch.value = batch
  } catch (e: any) {
    errorMessage.value = e?.response?.data?.message || '배치 등록 중 오류가 발생했습니다.'
  } finally {
    loading.value = false
  }
}

async function startRunning() {
  if (!createdBatch.value) return
  const res = await updateBatchStatus(createdBatch.value.batchId, 'RUNNING')
  createdBatch.value = res.data
}

onMounted(loadMasterData)
</script>
