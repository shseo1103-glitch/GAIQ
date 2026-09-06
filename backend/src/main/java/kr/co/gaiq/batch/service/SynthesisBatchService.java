package kr.co.gaiq.batch.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import kr.co.gaiq.audit.service.AuditRecorder;
import kr.co.gaiq.auth.principal.UserPrincipal;
import kr.co.gaiq.batch.dto.BatchStatusUpdateRequest;
import kr.co.gaiq.batch.dto.ProcessParamBatchCreateRequest;
import kr.co.gaiq.batch.dto.SynthesisBatchCreateRequest;
import kr.co.gaiq.batch.dto.SynthesisBatchDto;
import kr.co.gaiq.batch.dto.SynthesisProcessParamCreateRequest;
import kr.co.gaiq.batch.dto.SynthesisProcessParamDto;
import kr.co.gaiq.batch.entity.SynthesisBatch;
import kr.co.gaiq.batch.entity.SynthesisProcessParam;
import kr.co.gaiq.batch.repository.SynthesisBatchRepository;
import kr.co.gaiq.batch.repository.SynthesisBatchSpecifications;
import kr.co.gaiq.batch.repository.SynthesisProcessParamRepository;
import kr.co.gaiq.common.dto.PageResponse;
import kr.co.gaiq.common.exception.BusinessRuleViolationException;
import kr.co.gaiq.common.exception.DuplicateResourceException;
import kr.co.gaiq.common.exception.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Batch registration + process-param time series — implements "공정배치" tag of
 * {@code 01-gaiq-core.yaml}. All reads/writes are auto-scoped to the caller's org_id unless
 * the caller is PLATFORM_ADMIN (who may pass an explicit orgId filter).
 */
@Service
public class SynthesisBatchService {

    private static final String MODULE = "BATCH";

    private final SynthesisBatchRepository synthesisBatchRepository;
    private final SynthesisProcessParamRepository processParamRepository;
    private final AuditRecorder auditRecorder;

    public SynthesisBatchService(
            SynthesisBatchRepository synthesisBatchRepository,
            SynthesisProcessParamRepository processParamRepository,
            AuditRecorder auditRecorder) {
        this.synthesisBatchRepository = synthesisBatchRepository;
        this.processParamRepository = processParamRepository;
        this.auditRecorder = auditRecorder;
    }

    @Transactional(readOnly = true)
    public PageResponse<SynthesisBatchDto> list(
            UserPrincipal principal, Long orgIdParam, String batchNo, String status,
            LocalDate startDate, LocalDate endDate, Pageable pageable) {
        Long effectiveOrgId = resolveOrgIdFilter(principal, orgIdParam);
        Instant startInstant = startDate != null ? startDate.atStartOfDay(ZoneOffset.UTC).toInstant() : null;
        Instant endInstant = endDate != null ? endDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant() : null;
        Page<SynthesisBatch> page = synthesisBatchRepository.findAll(
                SynthesisBatchSpecifications.search(effectiveOrgId, batchNo, status, startInstant, endInstant),
                pageable);
        return PageResponse.of(page, SynthesisBatchDto::from);
    }

    @Transactional(readOnly = true)
    public SynthesisBatchDto get(UserPrincipal principal, Long batchId) {
        return SynthesisBatchDto.from(getScoped(principal, batchId));
    }

    @Transactional
    public SynthesisBatchDto create(UserPrincipal principal, SynthesisBatchCreateRequest request) {
        Long orgId = principal.orgId();
        if (synthesisBatchRepository.existsByOrgIdAndBatchNo(orgId, request.batchNo())) {
            throw new DuplicateResourceException("이미 존재하는 배치번호입니다: " + request.batchNo());
        }
        SynthesisBatch batch = new SynthesisBatch();
        batch.setOrgId(orgId);
        batch.setBatchNo(request.batchNo());
        batch.setEquipmentModelId(request.equipmentModelId());
        batch.setSubstrateId(request.substrateId());
        batch.setRawMaterialId(request.rawMaterialId());
        batch.setProcessType(request.processType());
        batch.setStartedAt(request.startedAt());
        batch.setStatus("PLANNED");
        batch.setOperatorUserId(principal.userId());
        batch.setRecipeVersion(request.recipeVersion());
        batch.setNotes(request.notes());
        synthesisBatchRepository.save(batch);

        auditRecorder.recordUserAction(
                orgId, principal.userId(), MODULE, "CREATE", "SynthesisBatch", batch.getBatchId(), null,
                Map.of("batchNo", batch.getBatchNo(), "status", batch.getStatus()));

        return SynthesisBatchDto.from(batch);
    }

    @Transactional
    public SynthesisBatchDto updateStatus(UserPrincipal principal, Long batchId, BatchStatusUpdateRequest request) {
        SynthesisBatch batch = getScoped(principal, batchId);
        String before = batch.getStatus();
        batch.setStatus(request.status());
        if ("RUNNING".equals(request.status()) && batch.getStartedAt() == null) {
            batch.setStartedAt(Instant.now());
        }
        if (("COMPLETED".equals(request.status()) || "FAILED".equals(request.status())
                || "ABORTED".equals(request.status())) && batch.getEndedAt() == null) {
            batch.setEndedAt(Instant.now());
        }

        auditRecorder.recordUserAction(
                batch.getOrgId(), principal.userId(), MODULE, "UPDATE", "SynthesisBatch", batchId,
                Map.of("status", before), Map.of("status", batch.getStatus()));

        return SynthesisBatchDto.from(batch);
    }

    @Transactional(readOnly = true)
    public List<SynthesisProcessParamDto> listProcessParams(
            UserPrincipal principal, Long batchId, String paramName) {
        getScoped(principal, batchId); // org 스코프 검증
        List<SynthesisProcessParam> params = paramName != null
                ? processParamRepository.findByBatchIdAndParamNameOrderByRecordedAtAsc(batchId, paramName)
                : processParamRepository.findByBatchIdOrderByRecordedAtAsc(batchId);
        return params.stream().map(SynthesisProcessParamDto::from).toList();
    }

    @Transactional
    public List<SynthesisProcessParamDto> createProcessParams(
            UserPrincipal principal, Long batchId, ProcessParamBatchCreateRequest request) {
        SynthesisBatch batch = getScoped(principal, batchId);

        List<SynthesisProcessParam> saved = request.params().stream().map(p -> {
            SynthesisProcessParam param = new SynthesisProcessParam();
            param.setBatchId(batchId);
            param.setParamName(p.paramName());
            param.setParamValue(p.paramValue());
            param.setUnit(p.unit());
            param.setRecordedAt(p.recordedAt() != null ? p.recordedAt() : Instant.now());
            return processParamRepository.save(param);
        }).toList();

        auditRecorder.recordUserAction(
                batch.getOrgId(), principal.userId(), MODULE, "CREATE", "SynthesisProcessParam", batchId, null,
                Map.of("count", saved.size()));

        return saved.stream().map(SynthesisProcessParamDto::from).toList();
    }

    /** Org 스코핑 검증: PLATFORM_ADMIN이 아닌 사용자는 자기 조직의 배치만 조회/수정 가능. */
    public SynthesisBatch getScoped(UserPrincipal principal, Long batchId) {
        SynthesisBatch batch = synthesisBatchRepository.findById(batchId)
                .orElseThrow(() -> new EntityNotFoundException("SynthesisBatch", batchId));
        if (!principal.isPlatformAdmin() && !batch.getOrgId().equals(principal.orgId())) {
            throw new BusinessRuleViolationException("ORG_SCOPE_VIOLATION", "다른 조직의 배치에 접근할 수 없습니다.");
        }
        return batch;
    }

    private Long resolveOrgIdFilter(UserPrincipal principal, Long orgIdParam) {
        if (principal.isPlatformAdmin()) {
            return orgIdParam; // null이면 전체 조직
        }
        return principal.orgId();
    }
}
