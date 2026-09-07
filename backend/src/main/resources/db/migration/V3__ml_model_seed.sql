-- ============================================================================
-- V3__ml_model_seed.sql — GAIQ ML 1단계 학습 모델 메타데이터 시드
-- 소스: ml/scripts/train_models.py 실행 결과 (ml/models/registry.json)
-- 대응 문서: docs/design/01-erd.md §6.1 ml_model_version, docs/recon/01-data-source-analysis.md §5
--
-- 학습 데이터: ml/data/processed/*.csv (experiment_dataset_raw.json 부스바 온도상승
-- row20-28, 9건). GPR(Matern kernel, LOO CV r2=0.9899)을 주모델(is_active=true)로,
-- RandomForest(LOO CV r2=0.8712)를 보조검증 모델(is_active=false)로 등록한다.
-- RAMAN_ID_IG/RAMAN_I2D_IG는 학습쌍이 5건 미만(각 0건/2건)이라 RULE_BASED_FALLBACK만 등록.
-- ============================================================================

INSERT INTO ml_model_version
    (target_metric_code, algorithm, version_no, trained_at, training_data_count,
     r2_score, mae, rmse, kernel_type, model_file_path, is_active, hyperparameters_json)
VALUES
    (
        'SURFACE_TEMP_C', 'GPR', 'v1-gpr', TIMESTAMP '2026-09-07 00:22:26.715', 9,
        0.9899, 0.7831, 1.0011,
        'Matern(nu=1.5)+WhiteKernel',
        '/home/work/.openclaw/workspace/gaiq/ml/models/SURFACE_TEMP_C_GPR_v1.pkl',
        TRUE,
        '{"features": ["current_A", "busbar_sq"], "n_restarts_optimizer": 8, "normalize_y": true}'::jsonb
    ),
    (
        'SURFACE_TEMP_C', 'RANDOM_FOREST', 'v1-rf', TIMESTAMP '2026-09-07 00:22:28.089', 9,
        0.8712, 3.2826, 3.5734,
        NULL,
        '/home/work/.openclaw/workspace/gaiq/ml/models/SURFACE_TEMP_C_RANDOM_FOREST_v1.pkl',
        FALSE,
        '{"features": ["current_A", "busbar_sq"], "n_estimators": 200, "max_depth": 4, "min_samples_leaf": 1}'::jsonb
    ),
    (
        'RAMAN_ID_IG', 'RULE_BASED_FALLBACK', 'v1', TIMESTAMP '2026-09-07 00:22:28.090', 0,
        NULL, NULL, NULL,
        NULL,
        'N/A_RULE_BASED_FALLBACK',
        TRUE,
        '{"features": ["temperature_C", "growth_time_min"], "reason": "training_data_count(0) < MIN_TRAINABLE(5)"}'::jsonb
    ),
    (
        'RAMAN_I2D_IG', 'RULE_BASED_FALLBACK', 'v1', TIMESTAMP '2026-09-07 00:22:28.091', 2,
        NULL, NULL, NULL,
        NULL,
        'N/A_RULE_BASED_FALLBACK',
        TRUE,
        '{"features": ["temperature_C", "growth_time_min"], "reason": "training_data_count(2) < MIN_TRAINABLE(5)"}'::jsonb
    );
