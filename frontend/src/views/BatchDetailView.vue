<!-- SCR-09 상세뷰: 공정파라미터 시계열 차트 + QC 측정값 + 라만 스펙트럼 뷰어 -->
<template>
  <div v-if="batch" class="mx-auto max-w-5xl space-y-6">
    <div class="surface-card flex items-center justify-between rounded-lg p-5">
      <div>
        <h2 class="text-lg font-semibold">{{ batch.batchNo }}</h2>
        <p class="text-xs text-slate-500 dark:text-slate-400">{{ batch.processType }} · 시작 {{ formatDate(batch.startedAt) }}</p>
      </div>
      <StatusBadge :status="batch.status" />
    </div>

    <div class="surface-card rounded-lg p-6">
      <h3 class="mb-3 text-sm font-semibold">공정 파라미터 시계열</h3>
      <div class="h-72">
        <Line v-if="paramChartData.datasets.length" :data="paramChartData" :options="paramChartOptions" />
        <p v-else class="flex h-full items-center justify-center text-xs text-slate-400">파라미터 데이터가 없습니다.</p>
      </div>
    </div>

    <div class="surface-card rounded-lg p-4">
      <h3 class="mb-3 text-sm font-semibold">QC 측정값</h3>
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
          <tr v-for="m in qcMeasurements" :key="m.qcMeasurementId" class="border-b border-slate-100 dark:border-slate-800">
            <td class="py-1.5">{{ m.metricCode }}</td>
            <td class="py-1.5 tabular-nums">{{ m.measuredValue ?? '-' }} {{ m.unit }}</td>
            <td class="py-1.5"><StatusBadge v-if="m.judgedResult" :status="m.judgedResult" /></td>
            <td class="py-1.5 text-xs text-slate-500">{{ formatDate(m.measuredAt) }}</td>
          </tr>
          <tr v-if="!qcMeasurements.length">
            <td colspan="4" class="py-4 text-center text-xs text-slate-400">측정값이 없습니다.</td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="surface-card rounded-lg p-6">
      <h3 class="mb-3 text-sm font-semibold">라만 스펙트럼 뷰어</h3>
      <div v-if="ramanSpectra.length" class="h-64">
        <Line :data="ramanChartData" :options="ramanChartOptions" />
      </div>
      <p v-else class="text-xs text-slate-400">등록된 라만 스펙트럼이 없습니다.</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { Line } from 'vue-chartjs'
import StatusBadge from '../components/StatusBadge.vue'
import { ensureChartRegistered } from '../composables/chartSetup'
import { getBatch, listProcessParams, listQcMeasurements, listRamanSpectra } from '../api/batches'
import type { QcMeasurement, RamanSpectrumRaw, SynthesisBatch, SynthesisProcessParam } from '../types'

ensureChartRegistered()

const props = defineProps<{ batchId: string }>()

const batch = ref<SynthesisBatch | null>(null)
const processParams = ref<SynthesisProcessParam[]>([])
const qcMeasurements = ref<QcMeasurement[]>([])
const ramanSpectra = ref<RamanSpectrumRaw[]>([])

const paramColors: Record<string, string> = {
  CHAMBER_TEMP_C: '#06b6d4',
  CH4_FLOW_SCCM: '#f97316',
  H2_FLOW_SCCM: '#8b5cf6',
  AR_FLOW_SCCM: '#22c55e',
  CHAMBER_PRESSURE_PA: '#eab308',
}

const paramChartData = computed(() => {
  const grouped: Record<string, SynthesisProcessParam[]> = {}
  for (const p of processParams.value) {
    grouped[p.paramName] = grouped[p.paramName] || []
    grouped[p.paramName].push(p)
  }
  const datasets = Object.entries(grouped).map(([name, items]) => ({
    label: name,
    data: items
      .sort((a, b) => new Date(a.recordedAt).getTime() - new Date(b.recordedAt).getTime())
      .map((i) => ({ x: i.recordedAt, y: i.paramValue })),
    borderColor: paramColors[name] || '#64748b',
    backgroundColor: paramColors[name] || '#64748b',
    tension: 0.2,
  }))
  return { datasets } as unknown as { datasets: any[] }
})
const paramChartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  scales: { x: { type: 'category' as const }, y: { beginAtZero: false } },
}

const ramanChartData = computed(() => {
  const s = ramanSpectra.value[0]
  if (!s) return { labels: [], datasets: [] }
  return {
    labels: s.wavenumberArray,
    datasets: [
      {
        label: `라만 스펙트럼 (${s.scanRangeCm1 || ''})`,
        data: s.intensityArray,
        borderColor: '#06b6d4',
        backgroundColor: 'rgba(6,182,212,0.15)',
        pointRadius: 0,
        tension: 0.1,
      },
    ],
  }
})
const ramanChartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  scales: {
    x: { title: { display: true, text: 'Raman shift (cm⁻¹)' } },
    y: { title: { display: true, text: 'Intensity' } },
  },
}

function formatDate(d?: string | null) {
  if (!d) return '-'
  return new Date(d).toLocaleString('ko-KR')
}

async function load() {
  const id = Number(props.batchId)
  const [b, params, qc, raman] = await Promise.all([
    getBatch(id),
    listProcessParams(id),
    listQcMeasurements(id),
    listRamanSpectra(id),
  ])
  batch.value = b.data
  processParams.value = params.data
  qcMeasurements.value = qc.data
  ramanSpectra.value = raman.data
}

load()
</script>
