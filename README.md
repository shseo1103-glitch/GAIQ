# GAIQ (Graphene AI-QC)

그래핀 CVD 합성 공정 데이터를 축적하고, 머신러닝 기반 물성예측·공정개선 진단을 제공하는 그래핀일렉트릭 플랫폼.

## 프로젝트 배경

- 클라이언트: 타 산업에서 그래핀을 도입을 원하는 업체
- 목표: CVD 합성 공정 파라미터(온도/압력/가스유량 등) → 물성 예측(라만 결함도/커버리지/전도도 등) ML 모델 기반 진단 시스템
- 1단계(MVP) 목표: 다양한 업체(그래핀일렉트릭 사내 및 타 산업에서 그래핀을 도입하는 업체), 룰기반+ML 하이브리드 진단, 배치/측정 데이터 DB화

> **2026-09-07 범위 변경**: 최초 결정(2026-09-07 새벽)은 "그래핀일렉트릭 단독 사내 시스템"이었으나, 사용자 지시로 **멀티테넌트(다양한 도입 업체) SaaS**로 범위 확대. 조직/사용자 관리, 데이터 격리(테넌트 분리)가 1단계 MVP부터 반영됨.

## 기술 스택

| 영역 | 기술 |
|---|---|
| 백엔드 | Spring Boot 3.3.4 / Java 21 |
| DB | PostgreSQL 16 |
| 프론트엔드 | Vue 3 + Vite |
| ML | Python (scikit-learn / XGBoost 등, 데이터 규모에 따라 결정) |
| 배포 | pm2, VM(20.194.5.165) |

## 디렉토리 구조

```
gaiq/
├── docs/
│   ├── recon/          # 원본 자료(첨부 47개 파일) 분석 결과
│   └── design/         # 아키텍처/ERD/화면명세/API계약
├── data/                # 실험 데이터셋 (raw/processed)
├── ml/                  # ML 모델 학습 파이프라인
├── backend/             # Spring Boot 백엔드
└── frontend/            # Vue3 프론트엔드
```

## 진행 상태

- [x] 요구사항 결정 완료 (2026-09-07)
- [x] 원본 데이터 분석 (`docs/recon/01-data-source-analysis.md`)
- [x] ERD/화면명세/API계약 설계 (`docs/design/`, `docs/api-contract/`)
- [x] 백엔드 구현 (Spring Boot, 46개 엔드포인트, JWT 인증, 멀티테넌트)
- [x] ML 모델 학습 (GPR 주모델 + RandomForest 보조검증, SURFACE_TEMP_C 실제학습완료)
- [x] 프론트엔드 구현 (Vue3, SCR-01~10 전체 10개 화면, 다크모드)
- [x] 통합 E2E 검증 (2026-09-07) — ML GPR모델 API미트리거 버그 발견/수정(V4 마이그레이션)
- [x] pm2 배포 (2026-09-07)

## 배포 현황 (2026-09-07)

| 서비스 | pm2 프로세스명 | 포트 | 실행 커맨드 |
|---|---|---|---|
| 백엔드 | `gaiq-backend` | 8089 | `java -jar build/libs/gaiq-backend.jar --server.port=8089` |
| 프론트엔드 | `gaiq-frontend` | 8114 | `npx vite --port 8114 --host` |
| ML 추론서비스 | `gaiq-ml` | 8115 | `.venv/bin/uvicorn service.main:app --host 0.0.0.0 --port 8115` (`--interpreter none` 필수) |

- DB: PostgreSQL `gaiq`(계정 gaiq/gaiq), localhost:5432
- 테스트 계정: orgCode=`GEL001`, loginId=`admin`, password=`admin1234`, role=PLATFORM_ADMIN
- `pm2 save` 완료. 단, systemd startup 서비스는 미등록 상태(이 VM의 다른 프로젝트들과 동일한 관례) — 서버 재부팅 시 `pm2 resurrect` 또는 수동 재기동 필요할 수 있음.
