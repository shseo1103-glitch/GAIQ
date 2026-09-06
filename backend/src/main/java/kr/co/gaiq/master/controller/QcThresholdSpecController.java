package kr.co.gaiq.master.controller;

import jakarta.validation.Valid;
import java.util.List;
import kr.co.gaiq.auth.principal.CurrentUserProvider;
import kr.co.gaiq.master.dto.QcThresholdSpecDto;
import kr.co.gaiq.master.dto.QcThresholdSpecUpsertRequest;
import kr.co.gaiq.master.service.MasterDataService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/qc-threshold-specs")
public class QcThresholdSpecController {

    private final MasterDataService masterDataService;
    private final CurrentUserProvider currentUserProvider;

    public QcThresholdSpecController(MasterDataService masterDataService, CurrentUserProvider currentUserProvider) {
        this.masterDataService = masterDataService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping
    public ResponseEntity<List<QcThresholdSpecDto>> list(
            @RequestParam(required = false) String metricCode,
            @RequestParam(required = false) String specType,
            @RequestParam(required = false) Long orgId) {
        return ResponseEntity.ok(masterDataService.listQcThresholdSpecs(metricCode, specType, orgId));
    }

    @PostMapping
    public ResponseEntity<QcThresholdSpecDto> create(@Valid @RequestBody QcThresholdSpecUpsertRequest request) {
        Long actorUserId = currentUserProvider.getOrThrow().userId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(masterDataService.createQcThresholdSpec(request, actorUserId));
    }
}
