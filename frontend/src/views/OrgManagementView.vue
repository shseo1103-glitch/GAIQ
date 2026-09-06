<!-- SCR-04: 조직/사용자 관리 (+ SCR-03 온보딩 승인 목록 겸용) -->
<template>
  <div class="space-y-6">
    <div class="flex gap-2 border-b border-slate-200 dark:border-slate-700/60">
      <button
        v-for="t in tabs"
        :key="t.key"
        class="px-4 py-2 text-sm font-medium border-b-2 -mb-px"
        :class="tab === t.key ? 'border-cyan-600 text-cyan-700 dark:text-cyan-400' : 'border-transparent text-slate-500 hover:text-slate-700 dark:text-slate-400'"
        @click="tab = t.key"
      >
        {{ t.label }}
      </button>
    </div>

    <!-- 온보딩 승인 대기 -->
    <div v-if="tab === 'pending'" class="surface-card rounded-lg p-4">
      <h3 class="mb-3 text-sm font-semibold">승인 대기중인 온보딩 신청</h3>
      <table class="w-full text-sm">
        <thead>
          <tr class="border-b border-slate-200 text-left text-xs text-slate-400 dark:border-slate-700/60">
            <th class="pb-2">조직명</th>
            <th class="pb-2">담당자</th>
            <th class="pb-2">이메일</th>
            <th class="pb-2">산업분류</th>
            <th class="pb-2 text-right">처리</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="org in pendingOrgs" :key="org.orgId" class="border-b border-slate-100 dark:border-slate-800">
            <td class="py-2 font-medium">{{ org.orgName }}</td>
            <td class="py-2">{{ org.contactName }}</td>
            <td class="py-2">{{ org.contactEmail }}</td>
            <td class="py-2">{{ org.industryType || '-' }}</td>
            <td class="py-2 text-right">
              <div class="flex justify-end gap-2">
                <button class="btn btn-primary !px-2 !py-1 text-xs" @click="approve(org)">승인</button>
                <button class="btn btn-danger !px-2 !py-1 text-xs" @click="openReject(org)">반려</button>
              </div>
            </td>
          </tr>
          <tr v-if="!pendingOrgs.length">
            <td colspan="5" class="py-6 text-center text-xs text-slate-400">대기중인 신청이 없습니다.</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 조직 목록 -->
    <div v-if="tab === 'orgs'" class="surface-card rounded-lg p-4">
      <h3 class="mb-3 text-sm font-semibold">조직 목록</h3>
      <table class="w-full text-sm">
        <thead>
          <tr class="border-b border-slate-200 text-left text-xs text-slate-400 dark:border-slate-700/60">
            <th class="pb-2">조직코드</th>
            <th class="pb-2">조직명</th>
            <th class="pb-2">유형</th>
            <th class="pb-2">상태</th>
            <th class="pb-2 text-right">동작</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="org in allOrgs" :key="org.orgId" class="border-b border-slate-100 dark:border-slate-800">
            <td class="py-2">{{ org.orgCode }}</td>
            <td class="py-2 font-medium">{{ org.orgName }}</td>
            <td class="py-2 text-xs text-slate-500">{{ org.orgType }}</td>
            <td class="py-2"><StatusBadge :status="org.status" /></td>
            <td class="py-2 text-right">
              <div class="flex justify-end gap-2">
                <button class="btn btn-secondary !px-2 !py-1 text-xs" @click="openUsers(org)">사용자 관리</button>
                <button
                  v-if="org.status === 'ACTIVE'"
                  class="btn btn-danger !px-2 !py-1 text-xs"
                  @click="setStatus(org, 'SUSPENDED')"
                >
                  정지
                </button>
                <button v-else-if="org.status === 'SUSPENDED'" class="btn btn-primary !px-2 !py-1 text-xs" @click="setStatus(org, 'ACTIVE')">
                  재활성화
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 반려 사유 모달 -->
    <AppModal v-model="rejectModalOpen" title="온보딩 반려">
      <div class="space-y-3">
        <p class="text-sm">{{ rejectTarget?.orgName }} 신청을 반려합니다.</p>
        <div>
          <label class="label">반려 사유 *</label>
          <textarea v-model="rejectReason" rows="3" class="input" />
        </div>
        <div class="flex justify-end gap-2">
          <button class="btn btn-secondary" @click="rejectModalOpen = false">취소</button>
          <button class="btn btn-danger" :disabled="!rejectReason" @click="confirmReject">반려 확정</button>
        </div>
      </div>
    </AppModal>

    <!-- 사용자 관리 모달 -->
    <AppModal v-model="usersModalOpen" :title="`${usersTarget?.orgName} - 사용자 관리`" width="lg">
      <div class="space-y-4">
        <table class="w-full text-sm">
          <thead>
            <tr class="border-b border-slate-200 text-left text-xs text-slate-400 dark:border-slate-700/60">
              <th class="pb-2">로그인ID</th>
              <th class="pb-2">이름</th>
              <th class="pb-2">역할</th>
              <th class="pb-2">상태</th>
              <th class="pb-2 text-right">동작</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="u in orgUsers" :key="u.userId" class="border-b border-slate-100 dark:border-slate-800">
              <td class="py-2">{{ u.loginId }}</td>
              <td class="py-2">{{ u.name }}</td>
              <td class="py-2">
                <select v-model="u.role" class="input !py-1 text-xs" @change="onRoleChange(u)">
                  <option value="PROCESS_ENGINEER">PROCESS_ENGINEER</option>
                  <option value="OPERATOR">OPERATOR</option>
                  <option value="PLATFORM_ADMIN">PLATFORM_ADMIN</option>
                </select>
              </td>
              <td class="py-2"><StatusBadge :status="u.status" /></td>
              <td class="py-2 text-right">
                <button class="btn btn-secondary !px-2 !py-1 text-xs" @click="resetPassword(u)">비번초기화</button>
              </td>
            </tr>
          </tbody>
        </table>

        <div class="border-t border-slate-200 pt-3 dark:border-slate-700/60">
          <h4 class="mb-2 text-sm font-semibold">신규 사용자 등록</h4>
          <form class="grid grid-cols-2 gap-3" @submit.prevent="createUser">
            <input v-model="newUser.loginId" required placeholder="로그인 ID" class="input" />
            <input v-model="newUser.password" type="password" required placeholder="초기 비밀번호" class="input" />
            <input v-model="newUser.name" required placeholder="이름" class="input" />
            <select v-model="newUser.role" class="input">
              <option value="PROCESS_ENGINEER">PROCESS_ENGINEER</option>
              <option value="OPERATOR">OPERATOR</option>
              <option value="PLATFORM_ADMIN">PLATFORM_ADMIN</option>
            </select>
            <input v-model="newUser.email" placeholder="이메일" class="input" />
            <button type="submit" class="btn btn-primary">등록</button>
          </form>
        </div>
      </div>
    </AppModal>

    <!-- role_permission 조회 (참고용, 조회 전용) -->
    <div v-if="tab === 'permissions'" class="surface-card rounded-lg p-4">
      <h3 class="mb-3 text-sm font-semibold">역할별 권한 매트릭스 (조회 전용)</h3>
      <table class="w-full text-sm">
        <thead>
          <tr class="border-b border-slate-200 text-left text-xs text-slate-400 dark:border-slate-700/60">
            <th class="pb-2">역할</th>
            <th class="pb-2">배치 등록</th>
            <th class="pb-2">QC 등록</th>
            <th class="pb-2">진단 조회</th>
            <th class="pb-2">조직관리</th>
            <th class="pb-2">감사로그</th>
          </tr>
        </thead>
        <tbody>
          <tr class="border-b border-slate-100 dark:border-slate-800">
            <td class="py-2 font-medium">PROCESS_ENGINEER</td>
            <td class="py-2"><CheckCircle2 class="h-4 w-4 text-green-600" /></td>
            <td class="py-2"><CheckCircle2 class="h-4 w-4 text-green-600" /></td>
            <td class="py-2"><CheckCircle2 class="h-4 w-4 text-green-600" /></td>
            <td class="py-2"><XCircle class="h-4 w-4 text-slate-300" /></td>
            <td class="py-2"><XCircle class="h-4 w-4 text-slate-300" /></td>
          </tr>
          <tr class="border-b border-slate-100 dark:border-slate-800">
            <td class="py-2 font-medium">OPERATOR</td>
            <td class="py-2"><CheckCircle2 class="h-4 w-4 text-green-600" /></td>
            <td class="py-2"><CheckCircle2 class="h-4 w-4 text-green-600" /></td>
            <td class="py-2"><CheckCircle2 class="h-4 w-4 text-green-600" /></td>
            <td class="py-2"><XCircle class="h-4 w-4 text-slate-300" /></td>
            <td class="py-2"><XCircle class="h-4 w-4 text-slate-300" /></td>
          </tr>
          <tr>
            <td class="py-2 font-medium">PLATFORM_ADMIN</td>
            <td class="py-2"><XCircle class="h-4 w-4 text-slate-300" /></td>
            <td class="py-2"><XCircle class="h-4 w-4 text-slate-300" /></td>
            <td class="py-2"><CheckCircle2 class="h-4 w-4 text-green-600" /></td>
            <td class="py-2"><CheckCircle2 class="h-4 w-4 text-green-600" /></td>
            <td class="py-2"><CheckCircle2 class="h-4 w-4 text-green-600" /></td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { CheckCircle2, XCircle } from 'lucide-vue-next'
import StatusBadge from '../components/StatusBadge.vue'
import AppModal from '../components/AppModal.vue'
import {
  approveOrganization,
  createOrgUser,
  listOrganizations,
  listOrgUsers,
  rejectOrganization,
  resetUserPassword,
  updateOrganization,
  updateUser,
} from '../api/organizations'
import type { Organization, UserAccount } from '../types'

const tabs = [
  { key: 'pending', label: '온보딩 승인 대기' },
  { key: 'orgs', label: '조직 목록' },
  { key: 'permissions', label: '권한 매트릭스' },
]
const tab = ref('pending')

const pendingOrgs = ref<Organization[]>([])
const allOrgs = ref<Organization[]>([])

async function loadPending() {
  const res = await listOrganizations({ status: 'PENDING', size: 100 })
  pendingOrgs.value = res.data.content
}
async function loadOrgs() {
  const res = await listOrganizations({ size: 100 })
  allOrgs.value = res.data.content
}

async function approve(org: Organization) {
  await approveOrganization(org.orgId)
  await loadPending()
}

const rejectModalOpen = ref(false)
const rejectTarget = ref<Organization | null>(null)
const rejectReason = ref('')
function openReject(org: Organization) {
  rejectTarget.value = org
  rejectReason.value = ''
  rejectModalOpen.value = true
}
async function confirmReject() {
  if (!rejectTarget.value) return
  await rejectOrganization(rejectTarget.value.orgId, rejectReason.value)
  rejectModalOpen.value = false
  await loadPending()
}

async function setStatus(org: Organization, status: 'ACTIVE' | 'SUSPENDED') {
  await updateOrganization(org.orgId, { status })
  await loadOrgs()
}

const usersModalOpen = ref(false)
const usersTarget = ref<Organization | null>(null)
const orgUsers = ref<UserAccount[]>([])
const newUser = ref({ loginId: '', password: '', name: '', role: 'OPERATOR' as UserAccount['role'], email: '' })

async function openUsers(org: Organization) {
  usersTarget.value = org
  usersModalOpen.value = true
  const res = await listOrgUsers(org.orgId, { size: 100 })
  orgUsers.value = res.data.content
}

async function onRoleChange(u: UserAccount) {
  await updateUser(u.userId, { role: u.role })
}

async function resetPassword(u: UserAccount) {
  await resetUserPassword(u.userId)
}

async function createUser() {
  if (!usersTarget.value) return
  await createOrgUser(usersTarget.value.orgId, { ...newUser.value })
  newUser.value = { loginId: '', password: '', name: '', role: 'OPERATOR', email: '' }
  await openUsers(usersTarget.value)
}

onMounted(() => {
  loadPending()
  loadOrgs()
})
</script>
