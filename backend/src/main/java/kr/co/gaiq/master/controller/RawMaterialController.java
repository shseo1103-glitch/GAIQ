package kr.co.gaiq.master.controller;

import jakarta.validation.Valid;
import kr.co.gaiq.auth.principal.CurrentUserProvider;
import kr.co.gaiq.common.dto.PageResponse;
import kr.co.gaiq.master.dto.RawMaterialDto;
import kr.co.gaiq.master.dto.RawMaterialUpsertRequest;
import kr.co.gaiq.master.service.MasterDataService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/raw-materials")
public class RawMaterialController {

    private final MasterDataService masterDataService;
    private final CurrentUserProvider currentUserProvider;

    public RawMaterialController(MasterDataService masterDataService, CurrentUserProvider currentUserProvider) {
        this.masterDataService = masterDataService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping
    public ResponseEntity<PageResponse<RawMaterialDto>> list(Pageable pageable) {
        return ResponseEntity.ok(masterDataService.listRawMaterials(pageable));
    }

    @PostMapping
    public ResponseEntity<RawMaterialDto> create(@Valid @RequestBody RawMaterialUpsertRequest request) {
        Long actorUserId = currentUserProvider.getOrThrow().userId();
        return ResponseEntity.status(HttpStatus.CREATED).body(masterDataService.createRawMaterial(request, actorUserId));
    }
}
