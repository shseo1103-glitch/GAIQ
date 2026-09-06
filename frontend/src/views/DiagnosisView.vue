<!-- SCR-07: AI 진단 결과 -->
<template>
  <div class="mx-auto max-w-4xl space-y-6">
    <div class="surface-card rounded-lg p-4">
      <label class="label">배치 선택</label>
      <select v-model.number="selectedBatchId" class="input" @change="loadDiagnosis">
        <option value="" disabled>배치를 선택하세요</option>
        <option v-for="b in batches" :key="b.batchId" :value="b.batchId">{{ b.batchNo }}</option>
      </select>
    </div>

    <template v-if="diagnosis">
      <div class="surface-card flex items-center justify-between rounded-lg p-6">
        <div>
          <p class="text-sm text-slate-500 dark:text-slate-400">종합 판정등급</p>
          <div class="mt-1"><StatusBadge :status="diagnosis.grade" /></div>
        </div>
        <RouterLink :to="`/batches/${selectedBatchId}`" class="btn btn-secondary text-xs">배치 상세 →</RouterLink>
      </div>

      <div class="surface-card rounded-lg p-6">
        <h3 class="mb-3 text-sm font-semibold">지표별 PASS/FAIL (룰기반)</h3>
        <table class="w-full text-sm">
          <thead>
            <tr class="border-b border-slate-200 text-left text-xs text-slate-400 dark:border-slate-700/60">
              <th class="pb-2">지표</th>
              <th class="pb-2">측정값</th>
              <th class="pb-2">적용기준</th>
              <th class="pb-2">판정</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="m in diagnosis.metricResults" :key="m.metricCode" class="border-b border-slate-100 dark:border-slate-800">
              <td class="py-2 font-medium">{{ m.metricCode }}</td>
              <td class="py-2 tabular-nums">{{ m.measuredValue ?? '-' }}</td>
              <td class="py-2 text-xs text-slate-500">{{ m.appliedSpecType }}</td>
              <td class="py-2"><StatusBadge :status="m.judgedResult" /></td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- ML 예측 vs 실측 미니 위젯 -->
      <div class="surface-card rounded-lg p-6">
        <div class="mb-3 flex items-center justify-between">
          <h3 class="text-sm font-semibold">ML 예측 vs 실측 (요약)</h3>
          <RouterLink to="/ml-report" class="text-xs text-cyan-600 hover:underline dark:text-cyan-400">
            상세 ML 진단 리포트 →
          </RouterLink>
        </div>
        <div v-if="mlPredictions.length" class="grid grid-cols-3 gap-4">
          <div v-for="p in mlPredictions" :key="p.predictionId" class="rounded-md bg-slate-50 p-3 dark:bg-elevation-2">
            <p class="text-xs text-slate-500">예측ID #{{ p.predictionId }}</p>
            <p class="text-lg font-bold tabular-nums">{{ p.predictedValue.toFixed(3) }}</p>
            <p v-if="p.actualValue != null" class="text-xs text-slate-400">실측: {{ p.actualValue }}</p>
            <StatusBadge v-if="p.confidenceLevel" :status="p.confidenceLevel" />
          </div>
        </div>
        <p v-else class="text-xs text-slate-400">ML 예측 데이터가 없습니다.</p>
      </div>

      <div class="surface-card rounded-lg p-6">
        <h3 class="mb-2 text-sm font-semibold">근본원인 분석</h3>
        <p class="text-sm leading-relaxed text-slate-600 dark:text-slate-300">{{ diagnosis.rootCauseSummary }}</p>
      </div>

      <div class="surface-card rounded-lg p-6">
        <h3 class="mb-3 text-sm font-semibold">레시피 개선안</h3>
        <table class="w-full text-sm">
          <thead>
            <tr class="border-b border-slate-200 text-left text-xs text-slate-400 dark:border-slate-700/60">
              <th class="pb-2">파라미터</th>
              <th class="pb-2">현재값</th>
              <th class="pb-2">권장값</th>
              <th class="pb-2">근거</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="s in diagnosis.improvementSuggestions" :key="s.paramName" class="border-b border-slate-100 dark:border-slate-800">
              <td class="py-2 font-medium">{{ s.paramName }}</td>
              <td class="py-2 tabular-nums">{{ s.currentValue ?? '-' }}</td>
              <td class="py-2 tabular-nums text-cyan-700 dark:text-cyan-400">{{ s.recommendedValue ?? '-' }}</td>
              <td class="py-2 text-xs text-slate-500">{{ s.rationale }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { RouterLink } from 'vue-router'
import StatusBadge from '../components/StatusBadge.vue'
import { listBatches, getBatchDiagnosis, listBatchMlPredictions } from '../api/batches'
import type { BatchDiagnosis, MlPredictionLog, SynthesisBatch } from '../types'

const batches = ref<SynthesisBatch[]>([])
const selectedBatchId = ref<number | ''>('')
const diagnosis = ref<BatchDiagnosis | null>(null)
const mlPredictions = ref<MlPredictionLog[]>([])

async function loadBatches() {
  const res = await listBatches({ size: 100, sort: ['createdAt,DESC'] })
  batches.value = res.data.content
}

async function loadDiagnosis() {
  if (!selectedBatchId.value) return
  const id = Number(selectedBatchId.value)
  const [d, m] = await Promise.all([getBatchDiagnosis(id), listBatchMlPredictions(id)])
  diagnosis.value = d.data
  mlPredictions.value = m.data
}

loadBatches()
</script>
