# GAIQ ML 1단계 — GPR 주모델 + RandomForest 보조검증

`docs/recon/01-data-source-analysis.md` §5 최종 권장안 구현.

## 설치

```bash
cd ml
python3 -m venv .venv
.venv/bin/pip install -r requirements.txt
```

## 실행 순서

```bash
# 1. 데이터 전처리 (CSV/JSON -> ml/data/processed/<metric_code>.csv)
.venv/bin/python3 scripts/prepare_dataset.py

# 2. 모델 학습 (GPR + RF, LOO CV, ml/models/*.pkl + registry.json)
.venv/bin/python3 scripts/train_models.py

# 3. FastAPI 추론 서비스 기동 (포트 8115)
.venv/bin/uvicorn service.main:app --host 0.0.0.0 --port 8115
```

## 학습된 모델 (2026-09-07 기준)

| metric_code | algorithm | n | r2 (LOO) | mae | rmse | 비고 |
|---|---|---|---|---|---|---|
| SURFACE_TEMP_C | GPR (주모델) | 9 | 0.9899 | 0.7831 | 1.0011 | 부스바 전류인가 온도상승(row20-28), features=[current_A, busbar_sq] |
| SURFACE_TEMP_C | RANDOM_FOREST (보조) | 9 | 0.8712 | 3.2826 | 3.5734 | 위와 동일 데이터 |
| RAMAN_ID_IG | RULE_BASED_FALLBACK | 0 | - | - | - | 정확 수치 매칭 0건 (부등호 표기만 존재) |
| RAMAN_I2D_IG | RULE_BASED_FALLBACK | 2 | - | - | - | 5건 미달, 모델 생성 안 함 |

## API

```
GET  /health
POST /predict  { "targetMetricCode": "SURFACE_TEMP_C", "inputParams": {"current_A": 200, "busbar_sq": 80} }
```

## 백엔드 연동

`backend/src/main/java/kr/co/gaiq/ml/client/MlInferenceClient.java` 가 이 서비스를 호출하며,
서비스 다운/모델없음 시 `MlPredictionService`가 자동으로 룰기반 폴백(최근 실측값 → threshold → 0)으로 전환한다.
`gaiq.ml.service-url` (기본 `http://localhost:8115`)로 설정 변경 가능.
