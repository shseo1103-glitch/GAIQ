package kr.co.gaiq.qc.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import kr.co.gaiq.audit.service.AuditRecorder;
import kr.co.gaiq.auth.principal.UserPrincipal;
import kr.co.gaiq.batch.entity.SynthesisBatch;
import kr.co.gaiq.batch.service.SynthesisBatchService;
import kr.co.gaiq.common.exception.BusinessRuleViolationException;
import kr.co.gaiq.qc.dto.RamanSpectrumRawDto;
import kr.co.gaiq.qc.entity.RamanSpectrumRaw;
import kr.co.gaiq.qc.repository.RamanSpectrumRawRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Raman spectrum raw CSV upload/parse — implements "품질측정" tag's raman-spectra paths of
 * {@code 01-gaiq-core.yaml}. Expects a 2-column CSV (wavenumber,intensity), header row optional.
 */
@Service
public class RamanSpectrumService {

    private static final String MODULE = "QC";

    private final RamanSpectrumRawRepository ramanSpectrumRawRepository;
    private final SynthesisBatchService synthesisBatchService;
    private final AuditRecorder auditRecorder;

    public RamanSpectrumService(
            RamanSpectrumRawRepository ramanSpectrumRawRepository,
            SynthesisBatchService synthesisBatchService,
            AuditRecorder auditRecorder) {
        this.ramanSpectrumRawRepository = ramanSpectrumRawRepository;
        this.synthesisBatchService = synthesisBatchService;
        this.auditRecorder = auditRecorder;
    }

    @Transactional(readOnly = true)
    public List<RamanSpectrumRawDto> list(UserPrincipal principal, Long batchId) {
        synthesisBatchService.getScoped(principal, batchId);
        return ramanSpectrumRawRepository.findByBatchIdOrderByMeasuredAtDesc(batchId).stream()
                .map(RamanSpectrumRawDto::from)
                .toList();
    }

    @Transactional
    public RamanSpectrumRawDto upload(
            UserPrincipal principal,
            Long batchId,
            MultipartFile file,
            Long measurementEquipmentId,
            Instant measuredAt) {
        SynthesisBatch batch = synthesisBatchService.getScoped(principal, batchId);

        List<Double> wavenumbers = new ArrayList<>();
        List<Double> intensities = new ArrayList<>();
        try (BufferedReader reader =
                new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            boolean first = true;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                String[] parts = line.split("[,\\t]");
                if (parts.length < 2) {
                    continue;
                }
                if (first) {
                    first = false;
                    if (!isNumeric(parts[0])) {
                        continue; // header row skip
                    }
                }
                try {
                    wavenumbers.add(Double.parseDouble(parts[0].trim()));
                    intensities.add(Double.parseDouble(parts[1].trim()));
                } catch (NumberFormatException ignored) {
                    // skip malformed rows
                }
            }
        } catch (IOException e) {
            throw new BusinessRuleViolationException("RAMAN_CSV_PARSE_FAILED", "CSV 파일 파싱에 실패했습니다: " + e.getMessage());
        }

        if (wavenumbers.isEmpty()) {
            throw new BusinessRuleViolationException("RAMAN_CSV_EMPTY", "유효한 데이터 행이 없습니다.");
        }

        RamanSpectrumRaw spectrum = new RamanSpectrumRaw();
        spectrum.setBatchId(batchId);
        spectrum.setMeasurementEquipmentId(measurementEquipmentId);
        spectrum.setWavenumberArray(wavenumbers);
        spectrum.setIntensityArray(intensities);
        spectrum.setScanRangeCm1(
                wavenumbers.get(0).intValue() + "-" + wavenumbers.get(wavenumbers.size() - 1).intValue());
        spectrum.setRawFilePath(file.getOriginalFilename());
        spectrum.setMeasuredAt(measuredAt != null ? measuredAt : Instant.now());
        ramanSpectrumRawRepository.save(spectrum);

        auditRecorder.recordUserAction(
                batch.getOrgId(), principal.userId(), MODULE, "CREATE", "RamanSpectrumRaw", spectrum.getSpectrumId(),
                null, Map.of("points", wavenumbers.size()));

        return RamanSpectrumRawDto.from(spectrum);
    }

    private boolean isNumeric(String s) {
        try {
            Double.parseDouble(s.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
