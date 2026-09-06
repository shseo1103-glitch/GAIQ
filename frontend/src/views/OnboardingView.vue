<!-- SCR-03: 테넌트(업체) 온보딩 - 신청 폼 (공개 접근) -->
<template>
  <div class="flex min-h-screen w-full items-center justify-center bg-slate-100 p-6 dark:bg-elevation-0">
    <div class="w-full max-w-lg surface-card rounded-xl p-8 shadow-lg">
      <div class="mb-6 text-center">
        <h1 class="text-xl font-bold">신규 업체 가입 신청</h1>
        <p class="mt-1 text-sm text-slate-500 dark:text-slate-400">
          그래핀 도입을 희망하는 업체 정보를 입력해주세요. 플랫폼 관리자 승인 후 로그인이 가능합니다.
        </p>
      </div>

      <div v-if="submitted" class="space-y-4 text-center">
        <CheckCircle2 class="mx-auto h-12 w-12 text-green-500" />
        <p class="text-sm">신청이 접수되었습니다. 관리자 승인을 기다려주세요.</p>
        <RouterLink :to="{ name: 'login' }" class="btn btn-primary inline-flex">로그인 화면으로</RouterLink>
      </div>

      <form v-else class="space-y-4" @submit.prevent="onSubmit">
        <div>
          <label class="label">조직명 *</label>
          <input v-model="form.orgName" required class="input" placeholder="예: OO전자" />
        </div>
        <div class="grid grid-cols-2 gap-4">
          <div>
            <label class="label">산업분류</label>
            <input v-model="form.industryType" class="input" placeholder="예: 반도체" />
          </div>
          <div>
            <label class="label">사업자등록번호</label>
            <input v-model="form.businessRegNo" class="input" placeholder="000-00-00000" />
          </div>
        </div>
        <div>
          <label class="label">담당자명 *</label>
          <input v-model="form.contactName" required class="input" />
        </div>
        <div class="grid grid-cols-2 gap-4">
          <div>
            <label class="label">담당자 이메일 *</label>
            <input v-model="form.contactEmail" type="email" required class="input" />
          </div>
          <div>
            <label class="label">담당자 연락처</label>
            <input v-model="form.contactPhone" class="input" />
          </div>
        </div>

        <div v-if="errorMessage" class="rounded-md bg-red-100 px-3 py-2 text-xs text-red-700 dark:bg-red-900/30 dark:text-red-300">
          {{ errorMessage }}
        </div>

        <button type="submit" :disabled="loading" class="btn btn-primary w-full">
          <Loader2 v-if="loading" class="h-4 w-4 animate-spin" />
          가입 신청
        </button>
        <RouterLink :to="{ name: 'login' }" class="block text-center text-xs text-slate-400 hover:underline">
          로그인 화면으로 돌아가기
        </RouterLink>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { CheckCircle2, Loader2 } from 'lucide-vue-next'
import { submitOnboardingRequest } from '../api/organizations'

const form = reactive({
  orgName: '',
  industryType: '',
  businessRegNo: '',
  contactName: '',
  contactEmail: '',
  contactPhone: '',
})
const loading = ref(false)
const submitted = ref(false)
const errorMessage = ref('')

async function onSubmit() {
  loading.value = true
  errorMessage.value = ''
  try {
    await submitOnboardingRequest(form)
    submitted.value = true
  } catch (e) {
    errorMessage.value = '신청 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.'
  } finally {
    loading.value = false
  }
}
</script>
