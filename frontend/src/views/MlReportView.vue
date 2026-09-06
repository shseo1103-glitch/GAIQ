<!-- SCR-08: ML 진단 리포트 -->
<template>
  <div class="mx-auto max-w-5xl space-y-6">
    <div class="surface-card grid grid-cols-2 gap-4 rounded-lg p-4">
      <div>
        <label class="label">대상 물성 지표 (targetMetricCode)</label>
        <input v-model="targetMetricCode" class="input" placeholder="예: ID_IG_RATIO" @keyup.enter="loadModels" />
      </div>
      <div class="flex items-end">
        <button class="btn btn-primary" @click="loadModels">모델 조회</button>
      </div>
    </div>

    <div v-if="models.length" class="surface-card rounded-lg p-4">
      <label class="label">모델 선택</label>
      <select v-model.number="selectedModelId" class="input" @change="onModelSelect">
        <option v-for="m in models" :key="m.modelId" :value="m.modelId">
          {{ m.algorithm }} v{{ m.versionNo }} (활성: {{ m.isActive ? 'Y' : 'N' }})
        </option>
      </select>
    </div>

    <!-- 데이터 부족 경고 배너: 상시 노출, 숨기지 않음 -->
    <div
      v-if="selectedModel && selectedModel.trainingDataCount < DATA_INSUFFICIENT_THRESHOLD"
      class="flex items-center gap-3 rounded-lg border-2 border-amber-400 bg-amber-50 p-4 dark:border-amber-500/60 dark:bg-amber-900/20"
    >
      <AlertTriangle class="h-6 w-6 shrink-0 text-amber-600 dark:text-amber-400" />
      <div>
        <p class="text-sm font-semibold text-amber-800 dark:text-amber-300">
          학습 데이터 부족 경고: 현재 학습 데이터 {{ selectedModel.trainingDataCount }}건
          (권장 최소 {{ DATA_INSUFFICIENT_THRESHOLD }}건)
        </p>
        <p class="text-xs text-amber-700 dark:text-amber-400">
          이 모델의 예측은 불확실성이 높을 수 있습니다. 참고용으로만 활용하고, 룰기반 판정(SCR-07)을 우선 확인하세요.
        </p>
      </div>
    </div>

    <template v-if="selectedModel">
      <!-- 모델 성능 카드 -->
      <div class="grid grid-cols-3 gap-4">
        <div class="surface-card rounded-lg p-4">
          <p class="text-xs text-slate-500">R² Score</p>
          <p class="text-2xl font-bold tabular-nums">{{ selectedModel.r2Score?.toFixed(3) ?? '-' }}</p>
        </div>
        <div class="surface-card rounded-lg p-4">
          <p class="text-xs text-slate-500">MAE</p>
          <p class="text-2xl font-bold tabular-nums">{{ selectedModel.mae?.toFixed(3) ?? '-' }}</p>
        </div>
        <div class="surface-card rounded-lg p-4">
          <p class="text-xs text-slate-500">학습 데이터 수</p>
          <p
            class="text-2xl font-bold tabular-nums"
            :class="selectedModel.trainingDataCount < DATA_INSUFFICIENT_THRESHOLD ? 'text-amber-600 dark:text-amber-400' : ''"
          >
            {{ selectedModel.trainingDataCount }}
          </p>
        </div>
      </div>

      <!-- 예측 vs 실측 산점도 -->
      <div class="surface-card rounded-lg p-6">
        <h3 class="mb-3 text-sm font-semibold">예측 vs 실측 산점도</h3>
        <div class="h-72">
          <Scatter v-if="scatterData.datasets[0].data.length" :data="scatterData" :options="scatterOptions" />
          <p v-else class="flex h-full items-center justify-center text-xs text-slate-400">예측 데이터가 없습니다.</p>
        </div>
      </div>

      <!-- 잔차 차트 -->
      <div class="surface-card rounded-lg p-6">
        <h3 class="mb-3 text-sm font-semibold">잔차(Residual) 차트</h3>
        <div class="h-64">
          <Scatter v-if="residualData.datasets[0].data.length" :data="residualData" :options="residualOptions" />
          <p v-else class="flex h-full items-center justify-center text-xs text-slate-400">잔차 데이터가 없습니다.</p>
        </div>
      </div>

      <!-- 개별 예측 상세 -->
      <div class="surface-card rounded-lg p-4">
        <h3 class="mb-3 text-sm font-semibold">개별 예측 상세</h3>
        <table class="w-full text-sm">
          <thead>
            <tr class="border-b border-slate-200 text-left text-xs text-slate-400 dark:border-slate-700/60">
              <th class="pb-2">배치ID</th>
              <th class="pb-2">예측값 ± 표준편차</th>
              <th class="pb-2">실측값</th>
              <th class="pb-2">잔차</th>
              <th class="pb-2">신뢰도</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="p in predictions" :key="p.predictionId" class="border-b border-slate-100 dark:border-slate-800">
              <td class="py-1.5">
                <RouterLink :to="`/batches/${p.batchId}`" class="text-cyan-600 hover:underline dark:text-cyan-400">
                  #{{ p.batchId }}
                </RouterLink>
              </td>
              <td class="py-1.5 tabular-nums">
                {{ p.predictedValue.toFixed(3) }}
                <span v-if="p.predictedStdDev != null" class="text-xs text-slate-400">± {{ p.predictedStdDev.toFixed(3) }}</span>
              </td>
              <td class="py-1.5 tabular-nums">{{ p.actualValue ?? '-' }}</td>
              <td class="py-1.5 tabular-nums">{{ p.residual ?? '-' }}</td>
              <td class="py-1.5"><StatusBadge v-if="p.confidenceLevel" :status="p.confidenceLevel" /></td>
            </tr>
          </tbody>
        </table>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { Scatter } from 'vue-chartjs'
import { AlertTriangle } from 'lucide-vue-next'
import StatusBadge from '../components/StatusBadge.vue'
import { ensureChartRegistered } from '../composables/chartSetup'
import { listMlModels, listMlModelPredictions } from '../api/ml'
import type { MlModelVersion, MlPredictionLog } from '../types'

ensureChartRegistered()

const DATA_INSUFFICIENT_THRESHOLD = 60

const targetMetricCode = ref('')
const models = ref<MlModelVersion[]>([])
const selectedModelId = ref<number | ''>('')
const selectedModel = computed(() => models.value.find((m) => m.modelId === selectedModelId.value) || null)
const predictions = ref<MlPredictionLog[]>([])

async function loadModels() {
  const res = await listMlModels({ targetMetricCode: targetMetricCode.value || undefined, activeOnly: false })
  models.value = res.data
  if (models.value.length) {
    selectedModelId.value = models.value[0].modelId
    await onModelSelect()
  } else {
    selectedModelId.value = ''
    predictions.value = []
  }
}

async function onModelSelect() {
  if (!selectedModelId.value) return
  const res = await listMlModelPredictions(Number(selectedModelId.value), { size: 200 })
  predictions.value = res.data.content
}

const scatterData = computed(() => ({
  datasets: [
    {
      label: '예측 vs 실측',
      data: predictions.value
        .filter((p) => p.actualValue != null)
        .map((p) => ({ x: p.actualValue as number, y: p.predictedValue })),
      backgroundColor: 'rgba(34, 211, 238, 0.7)',
    },
  ],
}))
const scatterOptions = {
  responsive: true,
  maintainAspectRatio: false,
  scales: {
    x: { title: { display: true, text: '실측값' } },
    y: { title: { display: true, text: '예측값' } },
  },
}

const residualData = computed(() => ({
  datasets: [
    {
      label: '잔차 (예측-실측)',
      data: predictions.value
        .filter((p) => p.residual != null)
        .map((p, i) => ({ x: i, y: p.residual as number })),
      backgroundColor: 'rgba(249, 115, 22, 0.7)',
    },
  ],
}))
const residualOptions = {
  responsive: true,
  maintainAspectRatio: false,
  scales: {
    x: { title: { display: true, text: '샘플 순번' } },
    y: { title: { display: true, text: '잔차' } },
  },
}
</script>
