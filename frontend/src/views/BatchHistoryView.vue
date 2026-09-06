<!-- SCR-09: 배치 이력 조회 -->
<template>
  <div class="space-y-4">
    <div class="surface-card grid grid-cols-5 gap-3 rounded-lg p-4">
      <input v-model="filters.batchNo" placeholder="배치번호" class="input" @keyup.enter="search" />
      <select v-model="filters.status" class="input">
        <option value="">전체 상태</option>
        <option v-for="s in statuses" :key="s" :value="s">{{ s }}</option>
      </select>
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
            <th class="px-4 py-2">배치번호</th>
            <th class="px-4 py-2">공정유형</th>
            <th class="px-4 py-2">시작</th>
            <th class="px-4 py-2">종료</th>
            <th class="px-4 py-2">상태</th>
            <th class="px-4 py-2"></th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="b in batches"
            :key="b.batchId"
            class="cursor-pointer border-b border-slate-100 hover:bg-slate-50 dark:border-slate-800 dark:hover:bg-elevation-2"
            @click="goDetail(b.batchId)"
          >
            <td class="px-4 py-2.5 font-medium">{{ b.batchNo }}</td>
            <td class="px-4 py-2.5 text-xs text-slate-500">{{ b.processType }}</td>
            <td class="px-4 py-2.5 text-xs text-slate-500">{{ formatDate(b.startedAt) }}</td>
            <td class="px-4 py-2.5 text-xs text-slate-500">{{ formatDate(b.endedAt) }}</td>
            <td class="px-4 py-2.5"><StatusBadge :status="b.status" /></td>
            <td class="px-4 py-2.5 text-right text-slate-400"><ChevronRight class="h-4 w-4" /></td>
          </tr>
          <tr v-if="!batches.length">
            <td colspan="6" class="py-8 text-center text-xs text-slate-400">조회된 배치가 없습니다.</td>
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
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Search, ChevronRight } from 'lucide-vue-next'
import StatusBadge from '../components/StatusBadge.vue'
import { listBatches } from '../api/batches'
import type { SynthesisBatch } from '../types'

const router = useRouter()
const statuses = ['PLANNED', 'RUNNING', 'COMPLETED', 'FAILED', 'ABORTED']

const filters = reactive({ batchNo: '', status: '', startDate: '', endDate: '' })
const batches = ref<SynthesisBatch[]>([])
const page = ref(0)
const totalPages = ref(0)
const totalElements = ref(0)

async function search() {
  page.value = 0
  await load()
}

async function load() {
  const res = await listBatches({
    page: page.value,
    size: 20,
    sort: ['createdAt,DESC'],
    batchNo: filters.batchNo || undefined,
    status: (filters.status || undefined) as any,
    startDate: filters.startDate || undefined,
    endDate: filters.endDate || undefined,
  })
  batches.value = res.data.content
  totalPages.value = res.data.totalPages
  totalElements.value = res.data.totalElements
}

function changePage(p: number) {
  page.value = p
  load()
}

function goDetail(id: number) {
  router.push(`/batches/${id}`)
}

function formatDate(d?: string | null) {
  if (!d) return '-'
  return new Date(d).toLocaleString('ko-KR')
}

load()
</script>
