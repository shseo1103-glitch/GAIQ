package kr.co.gaiq.master.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import kr.co.gaiq.audit.service.AuditRecorder;
import kr.co.gaiq.common.dto.PageResponse;
import kr.co.gaiq.common.exception.DuplicateResourceException;
import kr.co.gaiq.common.exception.EntityNotFoundException;
import kr.co.gaiq.master.dto.EquipmentModelDto;
import kr.co.gaiq.master.dto.EquipmentModelUpsertRequest;
import kr.co.gaiq.master.dto.MeasurementEquipmentDto;
import kr.co.gaiq.master.dto.MeasurementEquipmentUpsertRequest;
import kr.co.gaiq.master.dto.QcThresholdSpecDto;
import kr.co.gaiq.master.dto.QcThresholdSpecUpsertRequest;
import kr.co.gaiq.master.dto.RawMaterialDto;
import kr.co.gaiq.master.dto.RawMaterialUpsertRequest;
import kr.co.gaiq.master.dto.SubstrateDto;
import kr.co.gaiq.master.dto.SubstrateUpsertRequest;
import kr.co.gaiq.master.entity.EquipmentModel;
import kr.co.gaiq.master.entity.MeasurementEquipment;
import kr.co.gaiq.master.entity.QcThresholdSpec;
import kr.co.gaiq.master.entity.RawMaterial;
import kr.co.gaiq.master.entity.Substrate;
import kr.co.gaiq.master.repository.EquipmentModelRepository;
import kr.co.gaiq.master.repository.MeasurementEquipmentRepository;
import kr.co.gaiq.master.repository.QcThresholdSpecRepository;
import kr.co.gaiq.master.repository.RawMaterialRepository;
import kr.co.gaiq.master.repository.SubstrateRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CRUD for the 5 master-data entity types — implements the "마스터데이터" tag of
 * {@code 01-gaiq-core.yaml}. Master data is platform-wide (not org-scoped), except
 * qc_threshold_spec which may carry an optional org_id for org-specific overrides.
 */
@Service
public class MasterDataService {

    private static final String MODULE = "MASTER_DATA";

    private final EquipmentModelRepository equipmentModelRepository;
    private final RawMaterialRepository rawMaterialRepository;
    private final SubstrateRepository substrateRepository;
    private final MeasurementEquipmentRepository measurementEquipmentRepository;
    private final QcThresholdSpecRepository qcThresholdSpecRepository;
    private final AuditRecorder auditRecorder;

    public MasterDataService(
            EquipmentModelRepository equipmentModelRepository,
            RawMaterialRepository rawMaterialRepository,
            SubstrateRepository substrateRepository,
            MeasurementEquipmentRepository measurementEquipmentRepository,
            QcThresholdSpecRepository qcThresholdSpecRepository,
            AuditRecorder auditRecorder) {
        this.equipmentModelRepository = equipmentModelRepository;
        this.rawMaterialRepository = rawMaterialRepository;
        this.substrateRepository = substrateRepository;
        this.measurementEquipmentRepository = measurementEquipmentRepository;
        this.qcThresholdSpecRepository = qcThresholdSpecRepository;
        this.auditRecorder = auditRecorder;
    }

    // ---- EquipmentModel ----

    @Transactional(readOnly = true)
    public PageResponse<EquipmentModelDto> listEquipmentModels(String equipmentType, Pageable pageable) {
        Page<EquipmentModel> page = equipmentType != null
                ? equipmentModelRepository.findByEquipmentType(equipmentType, pageable)
                : equipmentModelRepository.findAll(pageable);
        return PageResponse.of(page, EquipmentModelDto::from);
    }

    @Transactional
    public EquipmentModelDto createEquipmentModel(EquipmentModelUpsertRequest request, Long actorUserId) {
        if (equipmentModelRepository.existsByModelCode(request.modelCode())) {
            throw new DuplicateResourceException("이미 존재하는 모델코드입니다: " + request.modelCode());
        }
        EquipmentModel e = new EquipmentModel();
        applyEquipmentModel(e, request);
        equipmentModelRepository.save(e);
        auditRecorder.recordUserAction(
                null, actorUserId, MODULE, "CREATE", "EquipmentModel", e.getEquipmentModelId(), null,
                Map.of("modelCode", e.getModelCode()));
        return EquipmentModelDto.from(e);
    }

    @Transactional
    public EquipmentModelDto updateEquipmentModel(
            Long id, EquipmentModelUpsertRequest request, Long actorUserId) {
        EquipmentModel e = equipmentModelRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("EquipmentModel", id));
        Map<String, Object> before = Map.of("modelName", e.getModelName());
        applyEquipmentModel(e, request);
        auditRecorder.recordUserAction(
                null, actorUserId, MODULE, "UPDATE", "EquipmentModel", id, before,
                Map.of("modelName", e.getModelName()));
        return EquipmentModelDto.from(e);
    }

    private void applyEquipmentModel(EquipmentModel e, EquipmentModelUpsertRequest request) {
        e.setModelCode(request.modelCode());
        e.setModelName(request.modelName());
        e.setEquipmentType(request.equipmentType());
        e.setProcessType(request.processType());
        e.setManufacturer(request.manufacturer());
        e.setSpecJson(request.specJson());
    }

    // ---- RawMaterial ----

    @Transactional(readOnly = true)
    public PageResponse<RawMaterialDto> listRawMaterials(Pageable pageable) {
        return PageResponse.of(rawMaterialRepository.findAll(pageable), RawMaterialDto::from);
    }

    @Transactional
    public RawMaterialDto createRawMaterial(RawMaterialUpsertRequest request, Long actorUserId) {
        if (rawMaterialRepository.existsByMaterialCode(request.materialCode())) {
            throw new DuplicateResourceException("이미 존재하는 원료코드입니다: " + request.materialCode());
        }
        RawMaterial r = new RawMaterial();
        r.setMaterialCode(request.materialCode());
        r.setMaterialName(request.materialName());
        r.setMaterialType(request.materialType());
        r.setPurityPct(request.purityPct());
        r.setLotNo(request.lotNo());
        r.setSupplier(request.supplier());
        rawMaterialRepository.save(r);
        auditRecorder.recordUserAction(
                null, actorUserId, MODULE, "CREATE", "RawMaterial", r.getRawMaterialId(), null,
                Map.of("materialCode", r.getMaterialCode()));
        return RawMaterialDto.from(r);
    }

    // ---- Substrate ----

    @Transactional(readOnly = true)
    public PageResponse<SubstrateDto> listSubstrates(String substrateType, Pageable pageable) {
        Page<Substrate> page = substrateType != null
                ? substrateRepository.findBySubstrateType(substrateType, pageable)
                : substrateRepository.findAll(pageable);
        return PageResponse.of(page, SubstrateDto::from);
    }

    @Transactional
    public SubstrateDto createSubstrate(SubstrateUpsertRequest request, Long actorUserId) {
        if (substrateRepository.existsBySubstrateCode(request.substrateCode())) {
            throw new DuplicateResourceException("이미 존재하는 기판코드입니다: " + request.substrateCode());
        }
        Substrate s = new Substrate();
        s.setSubstrateCode(request.substrateCode());
        s.setSubstrateType(request.substrateType());
        s.setSpecDesignation(request.specDesignation());
        s.setCrossSectionMm2(request.crossSectionMm2());
        s.setDiameterMm(request.diameterMm());
        s.setStrandCount(request.strandCount());
        substrateRepository.save(s);
        auditRecorder.recordUserAction(
                null, actorUserId, MODULE, "CREATE", "Substrate", s.getSubstrateId(), null,
                Map.of("substrateCode", s.getSubstrateCode()));
        return SubstrateDto.from(s);
    }

    // ---- MeasurementEquipment ----

    @Transactional(readOnly = true)
    public PageResponse<MeasurementEquipmentDto> listMeasurementEquipments(Pageable pageable) {
        return PageResponse.of(measurementEquipmentRepository.findAll(pageable), MeasurementEquipmentDto::from);
    }

    @Transactional
    public MeasurementEquipmentDto createMeasurementEquipment(
            MeasurementEquipmentUpsertRequest request, Long actorUserId) {
        if (measurementEquipmentRepository.existsByEquipmentCode(request.equipmentCode())) {
            throw new DuplicateResourceException("이미 존재하는 장비코드입니다: " + request.equipmentCode());
        }
        MeasurementEquipment m = new MeasurementEquipment();
        m.setEquipmentCode(request.equipmentCode());
        m.setEquipmentName(request.equipmentName());
        m.setEquipmentType(request.equipmentType());
        m.setManufacturer(request.manufacturer());
        m.setCalibrationDueDate(request.calibrationDueDate());
        measurementEquipmentRepository.save(m);
        auditRecorder.recordUserAction(
                null, actorUserId, MODULE, "CREATE", "MeasurementEquipment", m.getMeasurementEquipmentId(), null,
                Map.of("equipmentCode", m.getEquipmentCode()));
        return MeasurementEquipmentDto.from(m);
    }

    // ---- QcThresholdSpec ----

    @Transactional(readOnly = true)
    public List<QcThresholdSpecDto> listQcThresholdSpecs(String metricCode, String specType, Long orgId) {
        List<QcThresholdSpec> results;
        if (metricCode != null && specType != null) {
            results = qcThresholdSpecRepository.findByMetricCodeAndSpecType(metricCode, specType);
        } else if (metricCode != null && orgId != null) {
            results = qcThresholdSpecRepository.findByMetricCodeAndOrgId(metricCode, orgId);
        } else if (metricCode != null) {
            results = qcThresholdSpecRepository.findByMetricCode(metricCode);
        } else if (orgId != null) {
            results = qcThresholdSpecRepository.findByOrgId(orgId);
        } else {
            results = qcThresholdSpecRepository.findAll();
        }
        return results.stream().map(QcThresholdSpecDto::from).toList();
    }

    @Transactional
    public QcThresholdSpecDto createQcThresholdSpec(QcThresholdSpecUpsertRequest request, Long actorUserId) {
        QcThresholdSpec q = new QcThresholdSpec();
        q.setMetricCode(request.metricCode());
        q.setMetricName(request.metricName());
        q.setUnit(request.unit());
        q.setSpecType(request.specType());
        q.setComparator(request.comparator());
        q.setThresholdValue(request.thresholdValue());
        q.setThresholdValueMax(request.thresholdValueMax());
        q.setSourceDoc(request.sourceDoc());
        q.setOrgId(request.orgId());
        q.setEffectiveFrom(request.effectiveFrom());
        qcThresholdSpecRepository.save(q);
        auditRecorder.recordUserAction(
                request.orgId(), actorUserId, MODULE, "CREATE", "QcThresholdSpec", q.getThresholdId(), null,
                Map.of("metricCode", q.getMetricCode(), "specType", q.getSpecType()));
        return QcThresholdSpecDto.from(q);
    }
}
