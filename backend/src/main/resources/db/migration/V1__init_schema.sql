-- ============================================================================
-- V1__init_schema.sql — GAIQ 1단계(MVP) 전체 스키마
-- 소스: docs/design/01-erd.md (15개 정식 테이블 + certificate_coa skeleton)
-- ============================================================================

-- ----------------------------------------------------------------------------
-- A. 조직/사용자 (Tenant & RBAC)
-- ----------------------------------------------------------------------------

CREATE TABLE organization (
    org_id           BIGSERIAL PRIMARY KEY,
    org_code         VARCHAR(50)  NOT NULL UNIQUE,
    org_type         VARCHAR(30)  NOT NULL CHECK (org_type IN ('PRODUCER_GRAPHENE_ELECTRIC','ADOPTER','ADMIN')),
    org_name         VARCHAR(200) NOT NULL,
    industry_type    VARCHAR(100),
    business_reg_no  VARCHAR(50),
    status           VARCHAR(20)  NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING','ACTIVE','SUSPENDED')),
    contact_name     VARCHAR(100),
    contact_email    VARCHAR(200),
    contact_phone    VARCHAR(50),
    approved_by      BIGINT,
    approved_at      TIMESTAMP,
    created_at       TIMESTAMP NOT NULL DEFAULT now(),
    updated_at       TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE user_account (
    user_id        BIGSERIAL PRIMARY KEY,
    org_id         BIGINT       NOT NULL REFERENCES organization(org_id),
    login_id       VARCHAR(100) NOT NULL UNIQUE,
    password_hash  VARCHAR(255) NOT NULL,
    role           VARCHAR(30)  NOT NULL CHECK (role IN ('PROCESS_ENGINEER','OPERATOR','PLATFORM_ADMIN')),
    name           VARCHAR(100) NOT NULL,
    email          VARCHAR(200) UNIQUE,
    phone          VARCHAR(50),
    status         VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE','SUSPENDED')),
    last_login_at  TIMESTAMP,
    created_at     TIMESTAMP NOT NULL DEFAULT now(),
    updated_at     TIMESTAMP NOT NULL DEFAULT now()
);

ALTER TABLE organization
    ADD CONSTRAINT fk_organization_approved_by FOREIGN KEY (approved_by) REFERENCES user_account(user_id);

CREATE INDEX idx_user_account_org_id ON user_account(org_id);

CREATE TABLE role_permission (
    permission_id  BIGSERIAL PRIMARY KEY,
    role           VARCHAR(30) NOT NULL,
    module         VARCHAR(50) NOT NULL,
    can_view       BOOLEAN NOT NULL DEFAULT false,
    can_create     BOOLEAN NOT NULL DEFAULT false,
    can_edit       BOOLEAN NOT NULL DEFAULT false,
    can_delete     BOOLEAN NOT NULL DEFAULT false,
    UNIQUE (role, module)
);

-- ----------------------------------------------------------------------------
-- B. 마스터 데이터 (Metadata)
-- ----------------------------------------------------------------------------

CREATE TABLE equipment_model (
    equipment_model_id  BIGSERIAL PRIMARY KEY,
    model_code          VARCHAR(50)  NOT NULL UNIQUE,
    model_name          VARCHAR(200) NOT NULL,
    equipment_type      VARCHAR(30)  NOT NULL CHECK (equipment_type IN ('T_CVD','PECVD','LPCVD','APCVD','R2R','AIR_JET_MILL','OTHER')),
    process_type        VARCHAR(20)  NOT NULL CHECK (process_type IN ('BATCH','R2R')),
    manufacturer        VARCHAR(200),
    spec_json           JSONB,
    created_at          TIMESTAMP NOT NULL DEFAULT now(),
    updated_at          TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE raw_material (
    raw_material_id  BIGSERIAL PRIMARY KEY,
    material_code    VARCHAR(50)  NOT NULL UNIQUE,
    material_name    VARCHAR(200) NOT NULL,
    material_type    VARCHAR(30)  NOT NULL CHECK (material_type IN ('GAS','LIQUID_PRECURSOR','SOLID')),
    purity_pct       NUMERIC(6,3),
    lot_no           VARCHAR(100),
    supplier         VARCHAR(200),
    created_at       TIMESTAMP NOT NULL DEFAULT now(),
    updated_at       TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE substrate (
    substrate_id       BIGSERIAL PRIMARY KEY,
    substrate_code     VARCHAR(50) NOT NULL UNIQUE,
    substrate_type     VARCHAR(30) NOT NULL CHECK (substrate_type IN ('CU_WIRE','CU_STRAND','BUSBAR','SIO2_WAFER','SI_WAFER','GE_SI_WAFER','PET','GLASS','QUARTZ','OTHER')),
    spec_designation   VARCHAR(100),
    cross_section_mm2  NUMERIC(10,3),
    diameter_mm        NUMERIC(10,4),
    strand_count       INT,
    created_at         TIMESTAMP NOT NULL DEFAULT now(),
    updated_at         TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE measurement_equipment (
    measurement_equipment_id  BIGSERIAL PRIMARY KEY,
    equipment_code            VARCHAR(50)  NOT NULL UNIQUE,
    equipment_name            VARCHAR(200) NOT NULL,
    equipment_type            VARCHAR(30)  NOT NULL CHECK (equipment_type IN ('RAMAN','SEM','AFM','EBSD','FOUR_PROBE','IR_CAMERA','BULGE_TESTER','OTHER')),
    manufacturer              VARCHAR(200),
    calibration_due_date      DATE,
    created_at                TIMESTAMP NOT NULL DEFAULT now(),
    updated_at                TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE qc_threshold_spec (
    threshold_id       BIGSERIAL PRIMARY KEY,
    metric_code        VARCHAR(50)  NOT NULL,
    metric_name        VARCHAR(200) NOT NULL,
    unit               VARCHAR(30)  NOT NULL,
    spec_type          VARCHAR(30)  NOT NULL CHECK (spec_type IN ('TARGET_STAGE1','TARGET_STAGE2','VALIDATED_ACHIEVEMENT')),
    comparator         VARCHAR(10)  NOT NULL CHECK (comparator IN ('<=','>=','=','RANGE')),
    threshold_value     NUMERIC(12,4),
    threshold_value_max NUMERIC(12,4),
    source_doc         VARCHAR(300),
    org_id             BIGINT REFERENCES organization(org_id),
    effective_from     DATE,
    created_at         TIMESTAMP NOT NULL DEFAULT now(),
    updated_at         TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (metric_code, spec_type, org_id)
);

-- ----------------------------------------------------------------------------
-- C. 공정 실행 데이터 (Synthesis Batch)
-- ----------------------------------------------------------------------------

CREATE TABLE synthesis_batch (
    batch_id            BIGSERIAL PRIMARY KEY,
    org_id              BIGINT       NOT NULL REFERENCES organization(org_id),
    batch_no            VARCHAR(100) NOT NULL,
    equipment_model_id  BIGINT       NOT NULL REFERENCES equipment_model(equipment_model_id),
    substrate_id        BIGINT       NOT NULL REFERENCES substrate(substrate_id),
    raw_material_id     BIGINT       REFERENCES raw_material(raw_material_id),
    process_type        VARCHAR(20)  NOT NULL CHECK (process_type IN ('BATCH','R2R')),
    started_at          TIMESTAMP,
    ended_at            TIMESTAMP,
    status              VARCHAR(20)  NOT NULL DEFAULT 'PLANNED' CHECK (status IN ('PLANNED','RUNNING','COMPLETED','FAILED','ABORTED')),
    operator_user_id    BIGINT       REFERENCES user_account(user_id),
    recipe_version      VARCHAR(50),
    notes               TEXT,
    created_at          TIMESTAMP NOT NULL DEFAULT now(),
    updated_at          TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (org_id, batch_no)
);

CREATE INDEX idx_synthesis_batch_org_id ON synthesis_batch(org_id);

CREATE TABLE synthesis_process_param (
    param_id     BIGSERIAL PRIMARY KEY,
    batch_id     BIGINT NOT NULL REFERENCES synthesis_batch(batch_id),
    param_name   VARCHAR(50) NOT NULL CHECK (param_name IN ('CHAMBER_TEMP_C','CH4_FLOW_SCCM','H2_FLOW_SCCM','AR_FLOW_SCCM','CHAMBER_PRESSURE_PA','ANNEAL_TIME_MIN','GROWTH_TIME_MIN','ROLL_TENSION_KGM','WINDING_SPEED_MH','OTHER')),
    param_value  NUMERIC(14,4) NOT NULL,
    unit         VARCHAR(20) NOT NULL,
    recorded_at  TIMESTAMP NOT NULL DEFAULT now(),
    created_at   TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_synthesis_process_param_batch_name_time
    ON synthesis_process_param(batch_id, param_name, recorded_at);

-- ----------------------------------------------------------------------------
-- D. 품질측정 데이터 (QC Measurement)
-- ----------------------------------------------------------------------------

CREATE TABLE qc_measurement (
    qc_measurement_id       BIGSERIAL PRIMARY KEY,
    batch_id                BIGINT NOT NULL REFERENCES synthesis_batch(batch_id),
    metric_code             VARCHAR(50) NOT NULL,
    measured_value          NUMERIC(14,4),
    unit                    VARCHAR(30) NOT NULL,
    measurement_equipment_id BIGINT REFERENCES measurement_equipment(measurement_equipment_id),
    measured_at             TIMESTAMP NOT NULL,
    judged_result           VARCHAR(10) CHECK (judged_result IN ('PASS','FAIL')),
    judged_spec_type        VARCHAR(30) CHECK (judged_spec_type IN ('TARGET_STAGE1','TARGET_STAGE2','VALIDATED_ACHIEVEMENT')),
    measured_by_user_id     BIGINT REFERENCES user_account(user_id),
    notes                   TEXT,
    created_at              TIMESTAMP NOT NULL DEFAULT now(),
    updated_at              TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_qc_measurement_batch_id ON qc_measurement(batch_id);

CREATE TABLE raman_spectrum_raw (
    spectrum_id               BIGSERIAL PRIMARY KEY,
    batch_id                  BIGINT NOT NULL REFERENCES synthesis_batch(batch_id),
    measurement_equipment_id  BIGINT REFERENCES measurement_equipment(measurement_equipment_id),
    scan_range_cm1            VARCHAR(50),
    wavenumber_array          JSONB NOT NULL,
    intensity_array           JSONB NOT NULL,
    raw_file_path             VARCHAR(500),
    measured_at               TIMESTAMP NOT NULL,
    created_at                TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_raman_spectrum_raw_batch_id ON raman_spectrum_raw(batch_id);

-- ----------------------------------------------------------------------------
-- G. ML 예측/진단
-- ----------------------------------------------------------------------------

CREATE TABLE ml_model_version (
    model_id              BIGSERIAL PRIMARY KEY,
    target_metric_code    VARCHAR(50) NOT NULL,
    algorithm             VARCHAR(50) NOT NULL CHECK (algorithm IN ('GPR','RANDOM_FOREST','LINEAR_REGRESSION','RULE_BASED_FALLBACK')),
    version_no            VARCHAR(30) NOT NULL,
    trained_at            TIMESTAMP NOT NULL,
    training_data_count   INT NOT NULL,
    r2_score              NUMERIC(6,4),
    mae                   NUMERIC(14,4),
    rmse                  NUMERIC(14,4),
    kernel_type           VARCHAR(50),
    model_file_path       VARCHAR(500) NOT NULL,
    is_active             BOOLEAN NOT NULL DEFAULT false,
    hyperparameters_json  JSONB,
    created_at            TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (target_metric_code, version_no)
);

CREATE TABLE ml_prediction_log (
    prediction_id       BIGSERIAL PRIMARY KEY,
    batch_id            BIGINT NOT NULL REFERENCES synthesis_batch(batch_id),
    model_id            BIGINT NOT NULL REFERENCES ml_model_version(model_id),
    input_params_json   JSONB NOT NULL,
    predicted_value     NUMERIC(14,4) NOT NULL,
    predicted_std_dev   NUMERIC(14,4),
    confidence_level    VARCHAR(20) CHECK (confidence_level IN ('HIGH','MEDIUM','LOW','DATA_INSUFFICIENT')),
    actual_value        NUMERIC(14,4),
    residual            NUMERIC(14,4),
    predicted_at        TIMESTAMP NOT NULL DEFAULT now(),
    created_at           TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_ml_prediction_log_batch_id ON ml_prediction_log(batch_id);
CREATE INDEX idx_ml_prediction_log_model_id ON ml_prediction_log(model_id);

-- ----------------------------------------------------------------------------
-- F. 이력/감사 (Audit)
-- ----------------------------------------------------------------------------

CREATE TABLE action_log (
    action_log_id     BIGSERIAL PRIMARY KEY,
    org_id            BIGINT REFERENCES organization(org_id),
    actor_user_id     BIGINT REFERENCES user_account(user_id),
    actor_type        VARCHAR(20) NOT NULL CHECK (actor_type IN ('USER','SYSTEM')),
    module            VARCHAR(50) NOT NULL,
    action_type       VARCHAR(20) NOT NULL CHECK (action_type IN ('CREATE','UPDATE','DELETE','LOGIN','LOGOUT')),
    target_entity     VARCHAR(100) NOT NULL,
    target_entity_id  BIGINT,
    before_data       JSONB,
    after_data        JSONB,
    client_ip         VARCHAR(50),
    occurred_at       TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_action_log_org_id ON action_log(org_id);
CREATE INDEX idx_action_log_occurred_at ON action_log(occurred_at);

-- ----------------------------------------------------------------------------
-- 2단계 예정 skeleton 테이블: certificate_coa (1단계는 컬럼만 존재, API 미구현)
-- ----------------------------------------------------------------------------

CREATE TABLE certificate_coa (
    certificate_id          BIGSERIAL PRIMARY KEY,
    batch_id                BIGINT NOT NULL REFERENCES synthesis_batch(batch_id),
    issue_date              DATE,
    pdf_file_path           VARCHAR(500),
    blockchain_sha256_hash  VARCHAR(100), -- 2단계 예정: 컬럼만 존재, NULL 허용, 실구현 없음
    approver_user_id        BIGINT REFERENCES user_account(user_id),
    status                  VARCHAR(20),
    created_at              TIMESTAMP NOT NULL DEFAULT now(),
    updated_at              TIMESTAMP NOT NULL DEFAULT now()
);
