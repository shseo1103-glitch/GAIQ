-- ============================================================================
-- V2__seed_data.sql — 초기 시드 데이터
-- 소스: docs/design/01-erd.md §8 (qc_threshold_spec 시드), work-order 컨텍스트
-- ============================================================================

-- 1. 기본 조직: 그래핀일렉트릭 본사(PRODUCER)
INSERT INTO organization (org_code, org_type, org_name, industry_type, status)
VALUES ('GEL001', 'PRODUCER_GRAPHENE_ELECTRIC', '그래핀일렉트릭', NULL, 'ACTIVE');

-- 2. 플랫폼 관리자 계정 (login_id='admin', password='admin1234' BCrypt 해시)
-- BCrypt hash of 'admin1234' (strength 10)
INSERT INTO user_account (org_id, login_id, password_hash, role, name, status)
VALUES (
    (SELECT org_id FROM organization WHERE org_code = 'GEL001'),
    'admin',
    '$2b$10$hesTnKRccuScDQkV4v4Lf.uWqCbxf2DSqZIBLELISe2h2OXBIeRkK',
    'PLATFORM_ADMIN',
    '플랫폼관리자',
    'ACTIVE'
);

-- 3. qc_threshold_spec 시드 데이터 — §8.1 참고 목표치 (TARGET_STAGE1 / TARGET_STAGE2)
INSERT INTO qc_threshold_spec (metric_code, metric_name, unit, spec_type, comparator, threshold_value, source_doc, org_id, effective_from)
VALUES
    ('RAMAN_ID_IG', '라만 결함도(ID/IG)', 'ratio', 'TARGET_STAGE1', '<=', 0.13, '노트북LM 종합(제일테크노스/그래핀전선과제개요)', NULL, CURRENT_DATE),
    ('RAMAN_ID_IG', '라만 결함도(ID/IG)', 'ratio', 'TARGET_STAGE2', '<=', 0.10, '노트북LM 종합(제일테크노스/그래핀전선과제개요)', NULL, CURRENT_DATE),

    ('COVERAGE_PCT', '그래핀 커버리지', '%', 'TARGET_STAGE1', '>=', 90, '상동', NULL, CURRENT_DATE),
    ('COVERAGE_PCT', '그래핀 커버리지', '%', 'TARGET_STAGE2', '>=', 95, '상동', NULL, CURRENT_DATE),

    ('CONDUCTIVITY_IMPROVEMENT_PCT', '전기전도도 향상율', '%', 'TARGET_STAGE1', '>=', 7, '상동', NULL, CURRENT_DATE),
    ('CONDUCTIVITY_IMPROVEMENT_PCT', '전기전도도 향상율', '%', 'TARGET_STAGE2', '>=', 10, '상동', NULL, CURRENT_DATE),

    ('IACS_PCT', '도체 도전율', '%IACS', 'TARGET_STAGE1', '>=', 102, '상동', NULL, CURRENT_DATE),
    ('IACS_PCT', '도체 도전율', '%IACS', 'TARGET_STAGE2', '>=', 103, '상동', NULL, CURRENT_DATE),

    ('STRAND_SQ', '구리 연선 굵기', 'SQ(mm2)', 'TARGET_STAGE1', '<=', 10, '상동(제일테크노스는 25~40/60SQ로 상이 — 1MW급 별도 프로젝트, org 전용 스펙으로 별도 등록 권장)', NULL, CURRENT_DATE),
    ('STRAND_SQ', '구리 연선 굵기', 'SQ(mm2)', 'TARGET_STAGE2', '<=', 45, '상동(제일테크노스는 25~40/60SQ로 상이 — 1MW급 별도 프로젝트, org 전용 스펙으로 별도 등록 권장)', NULL, CURRENT_DATE),

    ('SURFACE_TEMP_C', '전류인가 표면온도', '℃', 'TARGET_STAGE2', '<=', 40, '상동(1단계 기준 없음)', NULL, CURRENT_DATE),

    ('CU_REDUCTION_PCT', '구리 사용 저감율', '%', 'TARGET_STAGE1', '>=', 5, '상동', NULL, CURRENT_DATE),
    ('CU_REDUCTION_PCT', '구리 사용 저감율', '%', 'TARGET_STAGE2', '>=', 10, '상동', NULL, CURRENT_DATE),

    ('CABLE_WEIGHT_REDUCTION_PCT', '케이블 무게 절감율', '%', 'TARGET_STAGE1', '>=', 2, '상동', NULL, CURRENT_DATE),
    ('CABLE_WEIGHT_REDUCTION_PCT', '케이블 무게 절감율', '%', 'TARGET_STAGE2', '>=', 5, '상동', NULL, CURRENT_DATE),

    ('EUV_TRANSMITTANCE_PCT', 'EUV 투과율(펠리클)', '%', 'TARGET_STAGE1', '>=', 85, '상동(펠리클 무관 프로젝트 — org 전용 스펙 후보)', NULL, CURRENT_DATE),
    ('EUV_TRANSMITTANCE_PCT', 'EUV 투과율(펠리클)', '%', 'TARGET_STAGE2', '>=', 90, '상동(펠리클 무관 프로젝트 — org 전용 스펙 후보)', NULL, CURRENT_DATE);

-- 4. qc_threshold_spec 시드 데이터 — §8.2 SSOT 실측치 (VALIDATED_ACHIEVEMENT)
INSERT INTO qc_threshold_spec (metric_code, metric_name, unit, spec_type, comparator, threshold_value, source_doc, org_id, effective_from)
VALUES
    ('RAMAN_ID_IG', '라만 D/G peak ratio', 'ratio', 'VALIDATED_ACHIEVEMENT', '=', 0.10, '2025년 창업중심대학 최종보고서 p.6', NULL, CURRENT_DATE);

-- STRAND_SQ already has UNIQUE(metric_code, spec_type, org_id) constraint with TARGET_STAGE1/2 above;
-- VALIDATED_ACHIEVEMENT is a distinct spec_type so no conflict.
INSERT INTO qc_threshold_spec (metric_code, metric_name, unit, spec_type, comparator, threshold_value, source_doc, org_id, effective_from)
VALUES
    ('STRAND_SQ', '연선 굵기(실제 제작사양)', 'SQ(mm2)', 'VALIDATED_ACHIEVEMENT', '=', 7, '상동 p.4', NULL, CURRENT_DATE),
    ('CABLE_TEMP_C', '충전케이블 온도', '℃', 'VALIDATED_ACHIEVEMENT', '=', 21, '상동 p.8, p.12 (개선율 22.2%)', NULL, CURRENT_DATE),
    ('CABLE_CROSS_SECTION_MM2', '케이블 단면적(경량화)', 'mm2', 'VALIDATED_ACHIEVEMENT', '=', 21.9, '상동 p.9, p.12 (경량화 10.2%)', NULL, CURRENT_DATE),
    ('INSULATION_RESISTANCE_GOHM_CP', '절연저항(L,N-CP)', 'GOhm', 'VALIDATED_ACHIEVEMENT', '=', 950, '상동 p.10, p.12 (개선율 281%)', NULL, CURRENT_DATE),
    ('INSULATION_RESISTANCE_GOHM_PE', '절연저항(L,N-PE)', 'GOhm', 'VALIDATED_ACHIEVEMENT', '=', 950, '상동 p.10 (동일)', NULL, CURRENT_DATE),
    ('DIELECTRIC_WITHSTAND_KV', '교류내전압(L,N-PE)', 'kV', 'VALIDATED_ACHIEVEMENT', '=', 1.42, '상동 p.11 (절연파괴 없음)', NULL, CURRENT_DATE);
