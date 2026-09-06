<!-- SCR-02: 대시보드. Glanceability 원칙 - 색상+숫자+아이콘 위주 KPI 카드 -->
<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <h2 class="text-lg font-semibold">공정 현황 대시보드</h2>
      <label v-if="auth.isPlatformAdmin" class="flex cursor-pointer items-center gap-2 text-sm">
        <span class="text-slate-500 dark:text-slate-400">전체 조직 합산 보기</span>
        <button
          class="relative h-6 w-11 rounded-full transition-colors"
          :class="auth.allOrgsView ? 'bg-cyan-600' : 'bg-slate-300 dark:bg-slate-600'"
          @click="onToggleAllOrgs"
        >
          <span
            class="absolute top-0.5 h-5 w-5 rounded-full bg-white shadow transition-transform"
            :class="auth.allOrgsView ? 'translate-x-5' : 'translate-x-0.5'"
          />
        </button>
      </label>
    </div>

    <!-- KPI 위젯 -->
    <div class="grid grid-cols-4 gap-4">
      <KpiCard label="진행중 배치" :value="summary.running" :icon="PlayCircle" tone="info" />
      <KpiCard label="완료 배치" :value="summary.completed" :icon="CheckCircle2" tone="pass" />
      <KpiCard label="실패 배치" :value="summary.failed" :icon="XCircle" tone="fail" />
      <KpiCard
        label="ML 신뢰도 경고"
        :value="mlAlerts.lowConfidenceCount + mlAlerts.dataInsufficientCount"
        :icon="AlertTriangle"
        :tone="mlAlerts.lowConfidenceCount + mlAlerts.dataInsufficientCount > 0 ? 'warn' : 'neutral'"
      />
    </div>

    <div class="grid grid-cols-2 gap-6">
      <!-- 최근 QC 판정결과 -->
      <div class="surface-card rounded-lg p-4">
        <h3 class="mb-3 text-sm font-semibold text-slate-600 dark:text-slate-300">최근 QC 판정결과</h3>
        <table class="w-full text-sm">
          <thead>
            <tr class="border-b border-slate-200 text-left text-xs text-slate-400 dark:border-slate-700/60">
              <th class="pb-2 font-medium">배치ID</th>
              <th class="pb-2 font-medium">지표</th>
              <th class="pb-2 font-medium">측정값</th>
              <th class="pb-2 font-medium">판정</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="qc in recentQc" :key="qc.qcMeasurementId" class="border-b border-slate-100 dark:border-slate-800">
              <td class="py-1.5">
                <RouterLink :to="`/batches/${qc.batchId}`" class="text-cyan-600 hover:underline dark:text-cyan-400">
                  #{{ qc.batchId }}
                </RouterLink>
              </td>
              <td class="py-1.5">{{ qc.metricCode }}</td>
              <td class="py-1.5 tabular-nums">{{ qc.measuredValue ?? '-' }} {{ qc.unit }}</td>
              <td class="py-1.5"><StatusBadge v-if="qc.judgedResult" :status="qc.judgedResult" /></td>
            </tr>
            <tr v-if="!recentQc.length">
              <td colspan="4" class="py-6 text-center text-xs text-slate-400">데이터가 없습니다.</td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 최근 배치 바로가기 -->
      <div class="surface-card rounded-lg p-4">
        <h3 class="mb-3 text-sm font-semibold text-slate-600 dark:text-slate-300">최근 배치</h3>
        <ul class="divide-y divide-slate-100 dark:divide-slate-800">
          <li v-for="b in recentBatches" :key="b.batchId" class="flex items-center justify-between py-2">
            <RouterLink :to="`/batches/${b.batchId}`" class="text-sm font-medium text-cyan-600 hover:underline dark:text-cyan-400">
              {{ b.batchNo }}
            </RouterLink>
            <StatusBadge :status="b.status" />
          </li>
          <li v-if="!recentBatches.length" class="py-6 text-center text-xs text-slate-400">배치 데이터가 없습니다.</li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { RouterLink } from 'vue-router'
import { PlayCircle, CheckCircle2, XCircle, AlertTriangle } from 'lucide-vue-next'
import KpiCard from '../components/KpiCard.vue'
import StatusBadge from '../components/StatusBadge.vue'
import { getBatchSummary, getMlConfidenceAlerts, getRecentQcResults, type BatchSummary, type MlConfidenceAlerts } from '../api/dashboard'
import { listBatches } from '../api/batches'
import { useAuthStore } from '../stores/auth'
import type { QcMeasurement, SynthesisBatch } from '../types'

const auth = useAuthStore()

const summary = ref<BatchSummary>({ planned: 0, running: 0, completed: 0, failed: 0 })
const mlAlerts = ref<MlConfidenceAlerts>({ lowConfidenceCount: 0, dataInsufficientCount: 0 })
const recentQc = ref<QcMeasurement[]>([])
const recentBatches = ref<SynthesisBatch[]>([])

async function loadAll() {
  const orgId = auth.isPlatformAdmin && auth.allOrgsView ? undefined : auth.isPlatformAdmin ? auth.user?.orgId : undefined
  const [summaryRes, alertsRes, qcRes, batchesRes] = await Promise.all([
    getBatchSummary(orgId),
    getMlConfidenceAlerts(orgId),
    getRecentQcResults(10, orgId),
    listBatches({ page: 0, size: 5, sort: ['createdAt,DESC'], orgId }),
  ])
  summary.value = summaryRes.data
  mlAlerts.value = alertsRes.data
  recentQc.value = qcRes.data
  recentBatches.value = batchesRes.data.content
}

function onToggleAllOrgs() {
  auth.toggleAllOrgsView()
}

watch(() => auth.allOrgsView, loadAll)
onMounted(loadAll)
</script>
