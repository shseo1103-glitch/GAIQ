<template>
  <div class="flex h-screen w-screen overflow-hidden">
    <!-- Sidebar -->
    <aside class="surface-sidebar flex w-56 shrink-0 flex-col">
      <div class="flex items-center gap-2 px-4 py-4 border-b border-slate-200 dark:border-slate-700/60">
        <FlaskConical class="h-6 w-6 text-cyan-600 dark:text-cyan-400" />
        <span class="text-lg font-bold tracking-tight">GAIQ</span>
      </div>
      <nav class="flex-1 space-y-0.5 overflow-y-auto px-2 py-3">
        <RouterLink
          v-for="item in navItems"
          :key="item.name"
          :to="item.to"
          class="flex items-center gap-3 rounded-md px-3 py-2 text-sm font-medium transition-colors"
          :class="isActive(item.to)
            ? 'bg-cyan-600/10 text-cyan-700 dark:bg-cyan-400/10 dark:text-cyan-300'
            : 'text-slate-600 hover:bg-slate-200/60 dark:text-slate-300 dark:hover:bg-elevation-3'"
        >
          <component :is="item.icon" class="h-4 w-4 shrink-0" />
          <span>{{ item.label }}</span>
        </RouterLink>
      </nav>
      <div class="px-4 py-3 text-[11px] text-slate-400 border-t border-slate-200 dark:border-slate-700/60">
        Graphene AI-QC · v0.1 MVP
      </div>
    </aside>

    <!-- Main -->
    <div class="flex min-w-0 flex-1 flex-col">
      <!-- Topbar -->
      <header class="surface-topbar flex h-14 shrink-0 items-center justify-between px-5">
        <div class="text-sm font-medium text-slate-500 dark:text-slate-400">
          {{ pageTitle }}
        </div>
        <div class="flex items-center gap-4">
          <div class="flex items-center gap-2 text-sm">
            <Building2 class="h-4 w-4 text-slate-400" />
            <span class="font-medium">{{ auth.user?.name }}</span>
            <span class="badge badge-info">{{ roleLabel }}</span>
          </div>
          <button
            class="rounded-full p-2 text-slate-500 hover:bg-slate-200/60 dark:text-slate-300 dark:hover:bg-elevation-3"
            :title="theme.isDark ? '라이트 모드로 전환' : '다크 모드로 전환'"
            @click="theme.toggle()"
          >
            <Sun v-if="theme.isDark" class="h-5 w-5" />
            <Moon v-else class="h-5 w-5" />
          </button>
          <button class="btn btn-secondary" @click="onLogout">
            <LogOut class="h-4 w-4" />
            로그아웃
          </button>
        </div>
      </header>

      <main class="flex-1 overflow-y-auto bg-surface-light p-6 dark:bg-elevation-0">
        <RouterView />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink, RouterView, useRoute, useRouter } from 'vue-router'
import {
  FlaskConical,
  LayoutDashboard,
  Building2,
  Users,
  Beaker,
  FlaskRound,
  Sparkles,
  LineChart,
  History,
  ScrollText,
  Sun,
  Moon,
  LogOut,
} from 'lucide-vue-next'
import { useAuthStore } from '../stores/auth'
import { useThemeStore } from '../stores/theme'

const auth = useAuthStore()
const theme = useThemeStore()
const route = useRoute()
const router = useRouter()

const roleLabelMap: Record<string, string> = {
  PLATFORM_ADMIN: '플랫폼관리자',
  PROCESS_ENGINEER: '공정엔지니어',
  OPERATOR: '오퍼레이터',
}
const roleLabel = computed(() => roleLabelMap[auth.role || ''] || auth.role)

const pageTitleMap: Record<string, string> = {
  dashboard: '대시보드',
  'org-management': '조직/사용자 관리',
  'batch-create': '공정 배치 등록',
  'qc-entry': '품질측정 데이터 등록',
  diagnosis: 'AI 진단 결과',
  'ml-report': 'ML 진단 리포트',
  'batch-history': '배치 이력 조회',
  'audit-log': '관리 행동 로그',
}
const pageTitle = computed(() => pageTitleMap[route.name as string] || '')

const allNavItems = [
  { name: 'dashboard', label: '대시보드', to: '/dashboard', icon: LayoutDashboard, roles: ['PLATFORM_ADMIN', 'PROCESS_ENGINEER', 'OPERATOR'] },
  { name: 'batch-create', label: '공정 배치 등록', to: '/batches/new', icon: Beaker, roles: ['PROCESS_ENGINEER', 'OPERATOR'] },
  { name: 'qc-entry', label: '품질측정 등록', to: '/qc/new', icon: FlaskRound, roles: ['PROCESS_ENGINEER', 'OPERATOR'] },
  { name: 'batch-history', label: '배치 이력 조회', to: '/batches', icon: History, roles: ['PLATFORM_ADMIN', 'PROCESS_ENGINEER', 'OPERATOR'] },
  { name: 'diagnosis', label: 'AI 진단 결과', to: '/diagnosis', icon: Sparkles, roles: ['PLATFORM_ADMIN', 'PROCESS_ENGINEER', 'OPERATOR'] },
  { name: 'ml-report', label: 'ML 진단 리포트', to: '/ml-report', icon: LineChart, roles: ['PLATFORM_ADMIN', 'PROCESS_ENGINEER'] },
  { name: 'org-management', label: '조직/사용자 관리', to: '/organizations', icon: Users, roles: ['PLATFORM_ADMIN'] },
  { name: 'audit-log', label: '관리 행동 로그', to: '/audit-logs', icon: ScrollText, roles: ['PLATFORM_ADMIN'] },
]

const navItems = computed(() => allNavItems.filter((item) => item.roles.includes(auth.role || '')))

function isActive(to: string) {
  return route.path === to || route.path.startsWith(to + '/')
}

function onLogout() {
  auth.logout()
  router.push({ name: 'login' })
}
</script>
