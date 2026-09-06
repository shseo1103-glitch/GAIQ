import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'login',
    component: () => import('../views/LoginView.vue'),
    meta: { public: true },
  },
  {
    path: '/onboarding',
    name: 'onboarding',
    component: () => import('../views/OnboardingView.vue'),
    meta: { public: true },
  },
  {
    path: '/',
    component: () => import('../layouts/AppLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      { path: '', redirect: '/dashboard' },
      { path: 'dashboard', name: 'dashboard', component: () => import('../views/DashboardView.vue') },
      {
        path: 'organizations',
        name: 'org-management',
        component: () => import('../views/OrgManagementView.vue'),
        meta: { roles: ['PLATFORM_ADMIN'] },
      },
      {
        path: 'batches/new',
        name: 'batch-create',
        component: () => import('../views/BatchCreateView.vue'),
        meta: { roles: ['PROCESS_ENGINEER', 'OPERATOR'] },
      },
      {
        path: 'batches',
        name: 'batch-history',
        component: () => import('../views/BatchHistoryView.vue'),
      },
      {
        path: 'batches/:batchId',
        name: 'batch-detail',
        component: () => import('../views/BatchDetailView.vue'),
        props: true,
      },
      {
        path: 'qc/new',
        name: 'qc-entry',
        component: () => import('../views/QcEntryView.vue'),
        meta: { roles: ['PROCESS_ENGINEER', 'OPERATOR'] },
      },
      {
        path: 'diagnosis',
        name: 'diagnosis',
        component: () => import('../views/DiagnosisView.vue'),
      },
      {
        path: 'ml-report',
        name: 'ml-report',
        component: () => import('../views/MlReportView.vue'),
        meta: { roles: ['PLATFORM_ADMIN', 'PROCESS_ENGINEER'] },
      },
      {
        path: 'audit-logs',
        name: 'audit-log',
        component: () => import('../views/AuditLogView.vue'),
        meta: { roles: ['PLATFORM_ADMIN'] },
      },
    ],
  },
  { path: '/:pathMatch(.*)*', redirect: '/dashboard' },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  if (to.meta.public) return true
  if (to.meta.requiresAuth !== false && !auth.isAuthenticated) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
  const roles = to.meta.roles as string[] | undefined
  if (roles && auth.role && !roles.includes(auth.role)) {
    return { name: 'dashboard' }
  }
  return true
})

export default router
