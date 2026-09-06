# 2단계-3. 화면 명세 설계 (Screen Spec)

> 기반 문서: `docs/design/00-work-order.md` §4(기능 요구사항), `docs/design/01-erd.md`(엔티티/테이블)
> 참고 형식: CSMS `docs/design/03-screen-spec.md` (구조만 참고, 내용은 GAIQ 신규 작성)
>
> 표기 규칙:
> - **화면ID**: `SCR-xx` 형식
> - **접근권한**: `docs/design/01-erd.md` §2.3 `role_permission`의 role 값(PROCESS_ENGINEER/OPERATOR/PLATFORM_ADMIN) 기준
> - **연동 API**: 다음 단계(`docs/api-contract/01-gaiq-core.yaml`)에서 구현할 API 경로 예상. 실제 계약 확정 시 조정될 수 있음
> - 1단계 MVP 화면만 상세 명세하며, 2단계 예정 화면은 이름/목적 1줄만 표기하고 상세 생략

---

## 목차

1. [1단계 MVP 화면 목록 (10개)](#1-1단계-mvp-화면-목록-10개)
2. [화면 상세 명세](#2-화면-상세-명세)
3. [2단계 예정 화면 (상세 생략)](#3-2단계-예정-화면-상세-생략)
4. [화면 수 요약](#4-화면-수-요약)

---

## 1. 1단계 MVP 화면 목록 (10개)

work order §4의 원안 11~13개 화면 중, 1단계 범위(멀티테넌트+룰기반/ML 하이브리드 진단, 레시피버전관리·LOT추적·CoA
제외)에 맞춰 **10개 화면**으로 확정한다. 원안 #7(레시피버전관리), #8(LOT추적), #9(CoA관리)는 2단계로 이연한다.

| # | 화면ID | 화면명 | 원안 대응 |
|---|---|---|---|
| 1 | SCR-01 | 로그인 | 원안 #1 |
| 2 | SCR-02 | 대시보드 | 원안 #2 |
| 3 | SCR-03 | 테넌트(업체) 온보딩 | 원안 #12 |
| 4 | SCR-04 | 조직/사용자 관리 | 원안 #10 |
| 5 | SCR-05 | 공정 배치 등록 | 원안 #3 |
| 6 | SCR-06 | 품질측정 데이터 등록 | 원안 #4 |
| 7 | SCR-07 | AI 진단 결과 | 원안 #5 |
| 8 | SCR-08 | ML 진단 리포트 | 원안 #13 |
| 9 | SCR-09 | 배치 이력 조회 | 원안 #6 |
| 10 | SCR-10 | 관리 행동 로그 | 원안 #11 |

---

## 2. 화면 상세 명세

### SCR-01. 로그인

- **화면ID**: SCR-01
- **화면명**: 로그인
- **목적**: 조직코드(org_code) + 로그인ID + 비밀번호로 인증하고 JWT를 발급받는다. 첨부 데모의 로그인 UI 톤(다크테마,
  cyan 포인트컬러)은 재사용하되, 실제 org_id 매핑 및 서버 인증 로직을 신규 구현한다.
- **접근권한**: 비로그인 상태(공개)
- **주요 UI요소**:
  - 조직코드 입력 필드, 로그인ID 입력 필드, 비밀번호 입력 필드
  - 로그인 버튼, 에러 메시지 영역(인증 실패/조직 비활성 등)
  - "신규 업체 가입 신청" 링크 → SCR-03(온보딩 신청) 이동
- **연동 API**:
  - `POST /api/v1/auth/login`
  - `GET /api/v1/auth/me` (토큰 검증 후 사용자/조직 정보 조회)
- **비고**: org_type=ADOPTER 조직은 status=ACTIVE(관리자 승인 완료)까지 로그인 차단.

### SCR-02. 대시보드

- **화면ID**: SCR-02
- **화면명**: 대시보드
- **목적**: 로그인한 조직(테넌트) 기준으로 배치별 진행현황, 최근 판정결과, 알림위젯을 한 화면에서 확인한다. CSMS
  대시보드 위젯 패턴(카운터+최근목록) 재사용.
- **접근권한**: PROCESS_ENGINEER, OPERATOR, PLATFORM_ADMIN (PLATFORM_ADMIN은 전체조직 합산 뷰 토글 가능)
- **주요 UI요소**:
  - 최근 배치 진행현황 위젯(진행중/완료/실패 건수)
  - 최근 QC 판정결과 목록(PASS/FAIL 배지)
  - ML 예측 신뢰도 경고 위젯(confidence_level=LOW/DATA_INSUFFICIENT 건수)
  - 최근 배치 바로가기 목록(SCR-09 연결)
- **연동 API**:
  - `GET /api/v1/dashboard/batches/summary`
  - `GET /api/v1/dashboard/qc-results/recent`
  - `GET /api/v1/dashboard/ml-confidence-alerts`
- **비고**: 조직별 데이터 격리(자사 org_id 필터) 필수. PLATFORM_ADMIN만 전체조직 조회 가능.

### SCR-03. 테넌트(업체) 온보딩

- **화면ID**: SCR-03
- **화면명**: 테넌트(업체) 온보딩
- **목적**: 신규 그래핀 도입 희망업체(ADOPTER)가 가입 신청하고, 플랫폼관리자가 승인/반려한다. 멀티테넌트 확장에 따른
  신규 화면(work order §4-12).
- **접근권한**: 신청 폼 자체는 비로그인 접근 가능(공개), 승인/반려 처리는 PLATFORM_ADMIN 전용
- **주요 UI요소**:
  - [신청 폼] 조직명/사업자등록번호/산업분류(industry_type)/담당자 정보 입력
  - [관리자 승인 목록] 대기중(PENDING) 조직 목록, 승인/반려 버튼, 반려사유 입력
- **연동 API**:
  - `POST /api/v1/organizations/onboarding-requests` (공개)
  - `GET /api/v1/organizations?status=PENDING` (PLATFORM_ADMIN)
  - `POST /api/v1/organizations/{orgId}/approve`
  - `POST /api/v1/organizations/{orgId}/reject`
- **비고**: 승인 시 `organization.status`가 ACTIVE로 전환되고 최초 관리자 계정(user_account) 생성 플로우와 연계.

### SCR-04. 조직/사용자 관리

- **화면ID**: SCR-04
- **화면명**: 조직/사용자 관리
- **목적**: 조직정보 조회/수정, 조직 내 사용자 계정 및 역할 CRUD를 수행한다. CSMS `관리자 설정` 패턴 재사용.
- **접근권한**: PLATFORM_ADMIN(전체 조직), 각 조직의 관리자 역할은 1단계에서 별도 구분 없이 PLATFORM_ADMIN만 처리
  (조직별 셀프서비스 관리자 위임은 2단계 검토)
- **주요 UI요소**:
  - 조직 목록/검색, 조직 상세(상태 변경: ACTIVE/SUSPENDED)
  - 조직별 사용자 목록, 사용자 등록/수정(역할 지정: PROCESS_ENGINEER/OPERATOR/PLATFORM_ADMIN), 비밀번호 초기화
  - role_permission 매트릭스 조회(1단계는 조회 위주, 수정 UI는 2단계 검토)
- **연동 API**:
  - `GET /api/v1/organizations`
  - `PUT /api/v1/organizations/{orgId}`
  - `GET /api/v1/organizations/{orgId}/users`
  - `POST /api/v1/organizations/{orgId}/users`
  - `PUT /api/v1/users/{userId}`
  - `POST /api/v1/users/{userId}/reset-password`

### SCR-05. 공정 배치 등록

- **화면ID**: SCR-05
- **화면명**: 공정 배치 등록
- **목적**: 장비/원료/기판을 선택하고 공정파라미터(챔버온도/가스유량/시간 등)를 입력하여 신규 배치를 등록한다.
  첨부 데모 STEP2 화면의 슬라이더 UI 골격은 참고하되, 실제 DB 저장/이력관리 로직을 신규 구현한다.
- **접근권한**: PROCESS_ENGINEER, OPERATOR
- **주요 UI요소**:
  - 장비모델 선택(equipment_model), 기판 선택(substrate, SQ값 표시), 원료 선택(raw_material)
  - 공정유형 토글(BATCH/R2R)
  - 공정파라미터 입력 폼: 챔버온도(150~2000℃)/CH4유량(0.15~200sccm)/H2유량(0.4~100sccm)/Ar유량(10~1000sccm)/
    챔버압력(0.6~6.6Pa)/어닐링시간(10~240min)/성장시간(5~120min)/(R2R 선택시) 롤장력(0.1~5kg/m)/권취속도(2~10m/h)
  - 저장 후 배치상태(PLANNED→RUNNING) 전환 버튼
- **연동 API**:
  - `GET /api/v1/equipment-models`, `GET /api/v1/substrates`, `GET /api/v1/raw-materials`
  - `POST /api/v1/batches`
  - `POST /api/v1/batches/{batchId}/process-params` (파라미터 일괄 등록)
  - `PATCH /api/v1/batches/{batchId}/status`
- **비고**: 파라미터 범위 밖 입력 시 경고(하드블록 아님, 실측 데이터 범위를 벗어난 외삽 가능성 안내).

### SCR-06. 품질측정 데이터 등록

- **화면ID**: SCR-06
- **화면명**: 품질측정 데이터 등록
- **목적**: 완료된 배치에 대해 라만 스펙트럼 CSV 업로드 또는 물성치 직접입력으로 QC 측정값을 등록한다.
- **접근권한**: PROCESS_ENGINEER, OPERATOR
- **주요 UI요소**:
  - 대상 배치 선택
  - 라만 스펙트럼 CSV 업로드(파일 드래그앤드롭) → 파싱 미리보기 → 저장
  - 물성치 직접입력 폼(metric_code 선택 + 측정값 + 측정장비 + 측정일시)
  - 룰기반 자동판정 결과 미리보기(PASS/FAIL, 적용 spec_type 표시)
- **연동 API**:
  - `POST /api/v1/batches/{batchId}/raman-spectra` (CSV 업로드)
  - `POST /api/v1/batches/{batchId}/qc-measurements`
  - `GET /api/v1/qc-threshold-specs?metricCode={code}` (판정기준 조회)
- **비고**: 저장 시 서버가 `qc_threshold_spec`과 대조해 `judged_result`/`judged_spec_type`을 자동 계산(룰기반 1차 판정).

### SCR-07. AI 진단 결과

- **화면ID**: SCR-07
- **화면명**: AI 진단 결과
- **목적**: 배치의 QC 측정값 기준 판정등급(A/B/C)과 근본원인 분석, 레시피 개선안을 제시한다. 1단계는 첨부 데모의
  룰기반 로직(4개 지표 중 통과 개수로 등급 산정)을 확장하고, ML 예측값(SCR-08 연계)을 함께 표시한다.
- **접근권한**: PROCESS_ENGINEER, OPERATOR, PLATFORM_ADMIN
- **주요 UI요소**:
  - 배치 선택 → 판정등급 배지(A/B/C), 지표별 PASS/FAIL 테이블(룰기반)
  - ML 예측 vs 실측 비교 미니 위젯(SCR-08로 상세 이동 링크)
  - 근본원인 분석 텍스트(룰기반 조건식 기반 서술) + 레시피 개선안 제안 테이블(파라미터명/현재값/권장값)
- **연동 API**:
  - `GET /api/v1/batches/{batchId}/diagnosis` (룰기반 판정 + 개선안)
  - `GET /api/v1/batches/{batchId}/ml-predictions` (ML 예측 요약)
- **비고**: 개선안은 1단계에서 규칙 기반 정적 매핑(예: ID/IG 초과 시 "성장시간 단축 권장")으로 시작, ML 기반 추천
  (`ai_recipe_recommendation`)은 2단계 예정.

### SCR-08. ML 진단 리포트

- **화면ID**: SCR-08
- **화면명**: ML 진단 리포트
- **목적**: GPR 주모델(+RandomForest 보조)의 예측물성 vs 실측물성을 비교하고, 모델 신뢰도(예측 표준편차/데이터
  부족 경고)를 투명하게 표시한다. work order §6 권장사항("모델 신뢰도/데이터 부족 경고" 화면 노출) 반영.
- **접근권한**: PROCESS_ENGINEER, PLATFORM_ADMIN
- **주요 UI요소**:
  - 모델 선택(target_metric_code별 활성 모델), 모델 성능 카드(r2_score/mae/training_data_count)
  - 예측 vs 실측 산점도/잔차(residual) 차트
  - 개별 예측 상세: predicted_value ± predicted_std_dev, confidence_level 배지(HIGH/MEDIUM/LOW/DATA_INSUFFICIENT)
  - "데이터 부족 경고" 배너(training_data_count < 60 등 임계치 미만 시 상시 노출)
- **연동 API**:
  - `GET /api/v1/ml-models?targetMetricCode={code}`
  - `GET /api/v1/ml-models/{modelId}/predictions`
  - `GET /api/v1/batches/{batchId}/ml-predictions/{predictionId}`
- **비고**: confidence_level 계산 로직은 predicted_std_dev 임계치 + training_data_count 임계치 조합(백엔드 구현 시 확정).

### SCR-09. 배치 이력 조회

- **화면ID**: SCR-09
- **화면명**: 배치 이력 조회
- **목적**: 과거 배치별 공정파라미터/측정값/판정결과를 목록·검색 조회한다.
- **접근권한**: PROCESS_ENGINEER, OPERATOR, PLATFORM_ADMIN
- **주요 UI요소**:
  - 검색 필터(배치번호/장비/기판/기간/상태/판정결과)
  - 목록 테이블(배치번호/장비/기판/시작~종료/상태/최종판정)
  - 상세보기: 공정파라미터 시계열 차트, QC 측정값 테이블, 라만 스펙트럼 뷰어
- **연동 API**:
  - `GET /api/v1/batches`
  - `GET /api/v1/batches/{batchId}`
  - `GET /api/v1/batches/{batchId}/process-params`
  - `GET /api/v1/batches/{batchId}/qc-measurements`
  - `GET /api/v1/batches/{batchId}/raman-spectra`

### SCR-10. 관리 행동 로그

- **화면ID**: SCR-10
- **화면명**: 관리 행동 로그
- **목적**: 모든 CRUD 변경 이력(before/after)을 검색/조회한다. CSMS `action_log` 패턴 그대로 재사용.
- **접근권한**: PLATFORM_ADMIN
- **주요 UI요소**:
  - 검색(행위자/모듈/기간/대상엔티티)
  - 목록, 변경전후(before_data/after_data) 상세보기 모달
- **연동 API**:
  - `GET /api/v1/audit/action-logs`
  - `GET /api/v1/audit/action-logs/{actionLogId}`

---

## 3. 2단계 예정 화면 (상세 생략)

| # | 화면명 | 목적(1줄) |
|---|---|---|
| 11 | 레시피 버전 관리 | 승인된 레시피 버전 이력 조회 및 롤백 |
| 12 | LOT 추적 조회 | 원자재 LOT → 생산배치 → 품질측정 계보 추적 |
| 13 | 시험성적서(CoA) 관리 | CoA 발급/조회/PDF 다운로드, 블록체인 해시 검증(2단계) |

---

## 4. 화면 수 요약

| 구분 | 개수 |
|---|---|
| 1단계 MVP 상세명세 화면 | 10 |
| 2단계 예정(상세 생략) 화면 | 3 |
| **합계(work order §4 원안 대응)** | **13** |

---

_다음 단계: `docs/api-contract/01-gaiq-core.yaml`(OpenAPI 계약 초안) 작성._
