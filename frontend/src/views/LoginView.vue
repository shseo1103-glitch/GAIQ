<!-- SCR-01: 로그인. 다크테마 cyan 포인트컬러 톤 -->
<template>
  <div class="flex h-screen w-screen items-center justify-center bg-slate-950">
    <div class="w-full max-w-sm rounded-xl border border-slate-800 bg-slate-900 p-8 shadow-2xl">
      <div class="mb-8 flex flex-col items-center gap-2">
        <div class="flex h-12 w-12 items-center justify-center rounded-full bg-cyan-500/10">
          <FlaskConical class="h-6 w-6 text-cyan-400" />
        </div>
        <h1 class="text-xl font-bold text-white">GAIQ</h1>
        <p class="text-xs text-slate-400">Graphene AI-QC 플랫폼</p>
      </div>

      <form class="space-y-4" @submit.prevent="onSubmit">
        <div>
          <label class="mb-1 block text-xs font-medium text-slate-400">조직코드</label>
          <input
            v-model="form.orgCode"
            type="text"
            required
            placeholder="예: GEL001"
            class="w-full rounded-md border border-slate-700 bg-slate-800 px-3 py-2 text-sm text-white placeholder-slate-500 focus:border-cyan-500 focus:outline-none focus:ring-1 focus:ring-cyan-500"
          />
        </div>
        <div>
          <label class="mb-1 block text-xs font-medium text-slate-400">로그인 ID</label>
          <input
            v-model="form.loginId"
            type="text"
            required
            class="w-full rounded-md border border-slate-700 bg-slate-800 px-3 py-2 text-sm text-white placeholder-slate-500 focus:border-cyan-500 focus:outline-none focus:ring-1 focus:ring-cyan-500"
          />
        </div>
        <div>
          <label class="mb-1 block text-xs font-medium text-slate-400">비밀번호</label>
          <input
            v-model="form.password"
            type="password"
            required
            class="w-full rounded-md border border-slate-700 bg-slate-800 px-3 py-2 text-sm text-white placeholder-slate-500 focus:border-cyan-500 focus:outline-none focus:ring-1 focus:ring-cyan-500"
          />
        </div>

        <div v-if="errorMessage" class="rounded-md bg-red-900/30 px-3 py-2 text-xs text-red-300">
          {{ errorMessage }}
        </div>

        <button
          type="submit"
          :disabled="loading"
          class="flex w-full items-center justify-center gap-2 rounded-md bg-cyan-500 px-4 py-2.5 text-sm font-semibold text-slate-950 transition-colors hover:bg-cyan-400 disabled:opacity-50"
        >
          <Loader2 v-if="loading" class="h-4 w-4 animate-spin" />
          로그인
        </button>
      </form>

      <div class="mt-6 text-center text-xs text-slate-500">
        도입을 원하는 신규 업체이신가요?
        <RouterLink :to="{ name: 'onboarding' }" class="font-medium text-cyan-400 hover:underline">
          신규 업체 가입 신청
        </RouterLink>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { FlaskConical, Loader2 } from 'lucide-vue-next'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const router = useRouter()
const route = useRoute()

const form = reactive({ orgCode: '', loginId: '', password: '' })
const loading = ref(false)
const errorMessage = ref('')

async function onSubmit() {
  loading.value = true
  errorMessage.value = ''
  try {
    await auth.login(form.orgCode, form.loginId, form.password)
    const redirect = (route.query.redirect as string) || '/dashboard'
    router.push(redirect)
  } catch (e: any) {
    if (e?.response?.status === 401) {
      errorMessage.value = '조직코드/로그인ID/비밀번호를 확인해주세요.'
    } else if (e?.response?.status === 403) {
      errorMessage.value = '조직이 아직 승인되지 않았거나 정지 상태입니다.'
    } else {
      errorMessage.value = '로그인 중 오류가 발생했습니다.'
    }
  } finally {
    loading.value = false
  }
}
</script>
