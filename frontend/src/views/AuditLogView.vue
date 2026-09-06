<!-- SCR-10: 관리 행동 로그 (PLATFORM_ADMIN 전용) -->
<template>
  <div class="space-y-4">
    <div class="surface-card grid grid-cols-5 gap-3 rounded-lg p-4">
      <input v-model="filters.module" placeholder="모듈" class="input" />
      <input v-model.number="filters.actorUserId" type="number" placeholder="행위자 ID" class="input" />
      <input v-model="filters.startDate" type="date" class="input" />
      <input v-model="filters.endDate" type="date" class="input" />
      <button class="btn btn-primary" @click="search">
        <Search class="h-4 w-4" /> 검색
      </button>
    </div>

    <div class="surface-card overflow-hidden rounded-lg">
      <table class="w-full text-sm">
        <thead>
          <tr class="border-b border-slate-200 bg-slate-50 text-left text-xs text-slate-500 dark:border-slate-700/60 dark:bg-elevation-2">
            <th class="px-4 py-2">일시</th>
            <th class="px-4 py-2">행위자</th>
            <th class="px-4 py-2">모듈</th>
            <th class="px-4 py-2">동작</th>
            <th class="px-4 py-2">대상</th>
            <th class="px-4 py-2"></th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="log in logs"
            :key="log.actionLogId"
            class="cursor-pointer border-b border-slate-100 hover:bg-slate-50 dark:border-slate-800 dark:hover:bg-elevation-2"
            @click="openDetail(log)"
          >
            <td class="px-4 py-2.5 text-xs text-slate-500">{{ formatDate(log.occurredAt) }}</td>
            <td class="px-4 py-2.5">{{ log.actorType === 'SYSTEM' ? 'SYSTEM' : `user#${log.actorUserId}` }}</td>
            <td class="px-4 py-2.5 text-xs">{{ log.module }}</td>
            <td class="px-4 py-2.5">
              <span class="badge badge-neutral">{{ log.actionType }}</span>
            </td>
            <td class="px-4 py-2.5 text-xs text-slate-500">{{ log.targetEntity }} #{{ log.targetEntityId }}</td>
            <td class="px-4 py-2.5 text-right text-slate-400"><ChevronRight class="h-4 w-4" /></td>
          </tr>
          <tr v-if="!logs.length">
            <td colspan="6" class="py-8 text-center text-xs text-slate-400">조회된 로그가 없습니다.</td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="flex items-center justify-between text-xs text-slate-500">
      <span>총 {{ totalElements }}건</span>
      <div class="flex gap-2">
        <button class="btn btn-secondary !px-2 !py-1" :disabled="page === 0" @click="changePage(page - 1)">이전</button>
        <span class="px-2 py-1">{{ page + 1 }} / {{ Math.max(totalPages, 1) }}</span>
        <button class="btn btn-secondary !px-2 !py-1" :disabled="page + 1 >= totalPages" @click="changePage(page + 1)">다음</button>
      </div>
    </div>

    <AppModal v-model="detailOpen" title="변경 이력 상세 (Before / After)" width="xl">
      <div v-if="detail" class="space-y-4">
        <div class="grid grid-cols-2 gap-3 text-xs text-slate-500">
          <div>행위자: {{ detail.actorType === 'SYSTEM' ? 'SYSTEM' : `user#${detail.actorUserId}` }}</div>
          <div>일시: {{ formatDate(detail.occurredAt) }}</div>
          <div>모듈: {{ detail.module }}</div>
          <div>대상: {{ detail.targetEntity }} #{{ detail.targetEntityId }}</div>
        </div>
        <div class="grid grid-cols-2 gap-4">
          <div>
            <p class="mb-1 text-xs font-semibold text-slate-500">Before</p>
            <pre class="max-h-96 overflow-auto rounded-md bg-slate-50 p-3 text-xs dark:bg-elevation-2">{{ pretty(detail.beforeData) }}</pre>
          </div>
          <div>
            <p class="mb-1 text-xs font-semibold text-slate-500">After</p>
            <pre class="max-h-96 overflow-auto rounded-md bg-slate-50 p-3 text-xs dark:bg-elevation-2">{{ pretty(detail.afterData) }}</pre>
          </div>
        </div>
      </div>
    </AppModal>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { Search, ChevronRight } from 'lucide-vue-next'
import AppModal from '../components/AppModal.vue'
import { getActionLog, listActionLogs } from '../api/audit'
import type { ActionLog } from '../types'

const filters = reactive({ module: '', actorUserId: undefined as number | undefined, startDate: '', endDate: '' })
const logs = ref<ActionLog[]>([])
const page = ref(0)
const totalPages = ref(0)
const totalElements = ref(0)

const detailOpen = ref(false)
const detail = ref<ActionLog | null>(null)

async function search() {
  page.value = 0
  await load()
}

async function load() {
  const res = await listActionLogs({
    page: page.value,
    size: 20,
    sort: ['occurredAt,DESC'],
    module: filters.module || undefined,
    actorUserId: filters.actorUserId || undefined,
    startDate: filters.startDate || undefined,
    endDate: filters.endDate || undefined,
  })
  logs.value = res.data.content
  totalPages.value = res.data.totalPages
  totalElements.value = res.data.totalElements
}

function changePage(p: number) {
  page.value = p
  load()
}

async function openDetail(log: ActionLog) {
  const res = await getActionLog(log.actionLogId)
  detail.value = res.data
  detailOpen.value = true
}

function pretty(data: unknown) {
  if (!data) return '(없음)'
  return JSON.stringify(data, null, 2)
}

function formatDate(d: string) {
  return new Date(d).toLocaleString('ko-KR')
}

load()
</script>
