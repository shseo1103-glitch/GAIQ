package kr.co.gaiq.master.controller;

import jakarta.validation.Valid;
import kr.co.gaiq.auth.principal.CurrentUserProvider;
import kr.co.gaiq.common.dto.PageResponse;
import kr.co.gaiq.master.dto.EquipmentModelDto;
import kr.co.gaiq.master.dto.EquipmentModelUpsertRequest;
import kr.co.gaiq.master.service.MasterDataService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/equipment-models")
public class EquipmentModelController {

    private final MasterDataService masterDataService;
    private final CurrentUserProvider currentUserProvider;

    public EquipmentModelController(MasterDataService masterDataService, CurrentUserProvider currentUserProvider) {
        this.masterDataService = masterDataService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping
    public ResponseEntity<PageResponse<EquipmentModelDto>> list(
            @RequestParam(required = false) String equipmentType, Pageable pageable) {
        return ResponseEntity.ok(masterDataService.listEquipmentModels(equipmentType, pageable));
    }

    @PostMapping
    public ResponseEntity<EquipmentModelDto> create(@Valid @RequestBody EquipmentModelUpsertRequest request) {
        Long actorUserId = currentUserProvider.getOrThrow().userId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(masterDataService.createEquipmentModel(request, actorUserId));
    }

    @PutMapping("/{equipmentModelId}")
    public ResponseEntity<EquipmentModelDto> update(
            @PathVariable Long equipmentModelId, @Valid @RequestBody EquipmentModelUpsertRequest request) {
        Long actorUserId = currentUserProvider.getOrThrow().userId();
        return ResponseEntity.ok(masterDataService.updateEquipmentModel(equipmentModelId, request, actorUserId));
    }
}
