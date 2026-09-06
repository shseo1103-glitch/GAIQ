# GAIQ Frontend (1단계 MVP)

Vue 3 + Vite + TypeScript 기반 GAIQ(Graphene AI-QC) 프론트엔드.

## 기술 스택

- Vue 3 (Composition API, `<script setup>`)
- Vite, TypeScript
- Pinia (상태관리: `useAuthStore`, `useThemeStore`)
- vue-router 4
- axios (JWT 인터셉터 + 401 자동 리다이렉트)
- Tailwind CSS 3 (`darkMode: 'class'`)
- Chart.js + vue-chartjs (공정파라미터 시계열, ML 예측 산점도/잔차)
- lucide-vue-next 아이콘

## 실행

```bash
npm install
cp .env.example .env   # VITE_API_BASE_URL 필요 시 수정
npm run dev            # http://localhost:8114
npm run build           # vue-tsc 타입체크 + vite build
```

백엔드(Spring Boot, `http://localhost:8089/api/v1`)가 먼저 기동되어 있어야 합니다.
테스트 계정: `orgCode=GEL001 loginId=admin password=admin1234` (PLATFORM_ADMIN)

## 화면 목록 (10개, `docs/design/02-screen-spec.md` SCR-01~10)

| 화면ID | 경로 | 파일 |
|---|---|---|
| SCR-01 | `/login` | `src/views/LoginView.vue` |
| SCR-02 | `/dashboard` | `src/views/DashboardView.vue` |
| SCR-03 | `/onboarding` (신청) + `/organizations` 탭(승인) | `src/views/OnboardingView.vue`, `OrgManagementView.vue` |
| SCR-04 | `/organizations` | `src/views/OrgManagementView.vue` |
| SCR-05 | `/batches/new` | `src/views/BatchCreateView.vue` |
| SCR-06 | `/qc/new` | `src/views/QcEntryView.vue` |
| SCR-07 | `/diagnosis` | `src/views/DiagnosisView.vue` |
| SCR-08 | `/ml-report` | `src/views/MlReportView.vue` |
| SCR-09 | `/batches`, `/batches/:batchId` | `src/views/BatchHistoryView.vue`, `BatchDetailView.vue` |
| SCR-10 | `/audit-logs` | `src/views/AuditLogView.vue` |

## 다크모드

- `useThemeStore`(Pinia)가 `localStorage['gaiq-theme']`에 사용자 선택을 저장하고,
  최초 진입 시 `prefers-color-scheme: dark` OS 설정을 기본값으로 사용합니다.
- 상단바(AppLayout)의 해/달 아이콘 버튼으로 토글하며, `<html>` 엘리먼트에 `dark` 클래스를 추가/제거합니다.
- Material Design 3 다크테마 가이드라인 반영: 순수 검정(#000) 대신 `#121212`~`#2c2c2c` 계열 엘리베이션 표면색
  (`tailwind.config.js`의 `elevation.0~5`) 사용, 카드/모달/탑바/사이드바별로 미세한 밝기 단계 차이를 줌.
- PASS/FAIL/경고/신뢰도 배지 색상은 다크모드에서 채도를 낮춘 톤(`dark:bg-*-900/40` + `dark:text-*-300`)을 사용해
  눈부심을 방지하되, 상태 판별력은 유지합니다.

## API 클라이언트

`src/api/*.ts`는 `docs/api-contract/01-gaiq-core.yaml`(46개 엔드포인트)에서 필요한 부분만 발췌해 타이핑했습니다.
공통 axios 인스턴스(`src/api/http.ts`)가 JWT를 자동 첨부하고 401 응답 시 로그인 페이지로 리다이렉트합니다.
