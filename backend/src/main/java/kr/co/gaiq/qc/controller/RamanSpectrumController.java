package kr.co.gaiq.qc.controller;

import java.time.Instant;
import java.util.List;
import kr.co.gaiq.auth.principal.CurrentUserProvider;
import kr.co.gaiq.qc.dto.RamanSpectrumRawDto;
import kr.co.gaiq.qc.service.RamanSpectrumService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Implements "품질측정" tag's raman-spectra paths of {@code 01-gaiq-core.yaml}.
 */
@RestController
@RequestMapping("/api/v1/batches/{batchId}/raman-spectra")
public class RamanSpectrumController {

    private final RamanSpectrumService ramanSpectrumService;
    private final CurrentUserProvider currentUserProvider;

    public RamanSpectrumController(RamanSpectrumService ramanSpectrumService, CurrentUserProvider currentUserProvider) {
        this.ramanSpectrumService = ramanSpectrumService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping
    public ResponseEntity<List<RamanSpectrumRawDto>> list(@PathVariable Long batchId) {
        var principal = currentUserProvider.getOrThrow();
        return ResponseEntity.ok(ramanSpectrumService.list(principal, batchId));
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<RamanSpectrumRawDto> upload(
            @PathVariable Long batchId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) Long measurementEquipmentId,
            @RequestParam(required = false) Instant measuredAt) {
        var principal = currentUserProvider.getOrThrow();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ramanSpectrumService.upload(principal, batchId, file, measurementEquipmentId, measuredAt));
    }
}
