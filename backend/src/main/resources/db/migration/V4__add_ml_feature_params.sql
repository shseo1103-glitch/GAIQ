-- V4: synthesis_process_param.param_name CHECK 제약에 ML(SURFACE_TEMP_C GPR) 모델이
-- 실제로 요구하는 피처(CURRENT_A, BUSBAR_SQ)를 추가한다.
--
-- 배경: ml/models/registry.json의 SURFACE_TEMP_C GPR 모델은 features=["current_A","busbar_sq"]로
-- 학습되었으나, 기존 V1 스키마의 param_name CHECK 제약에는 CVD 공정변수(CHAMBER_TEMP_C 등)만
-- 등록되어 있어 API(POST /batches/{id}/process-params)로는 이 두 피처를 입력할 방법이 없었다.
-- 그 결과 /batches/{id}/ml-predictions 호출 시 MlInferenceClient가 항상
-- "Missing required input features: ['current_A', 'busbar_sq']"를 반환하여 실제 GPR 모델이
-- 절대 트리거되지 않고 매번 룰기반 폴백으로만 빠지는 구조적 결함이 있었다(2026-09-07 통합검증 중 발견).

ALTER TABLE synthesis_process_param DROP CONSTRAINT synthesis_process_param_param_name_check;

-- 주의: 값은 ML FastAPI 서비스(ml/models/registry.json)가 기대하는 피처명(current_A, busbar_sq)과
-- 대소문자까지 정확히 일치시킨다(대문자로 바꾸면 predict_service.py의 feature lookup이 실패함).
ALTER TABLE synthesis_process_param ADD CONSTRAINT synthesis_process_param_param_name_check
    CHECK (param_name IN (
        'CHAMBER_TEMP_C', 'CH4_FLOW_SCCM', 'H2_FLOW_SCCM', 'AR_FLOW_SCCM', 'CHAMBER_PRESSURE_PA',
        'ANNEAL_TIME_MIN', 'GROWTH_TIME_MIN', 'ROLL_TENSION_KGM', 'WINDING_SPEED_MH',
        'current_A', 'busbar_sq',
        'OTHER'
    ));

COMMENT ON CONSTRAINT synthesis_process_param_param_name_check ON synthesis_process_param IS
    'V4: current_A/busbar_sq 추가 - SURFACE_TEMP_C GPR 모델의 실제 학습 피처를 API로 입력 가능하게 함(ML서비스 피처명과 대소문자 일치 필수)';
