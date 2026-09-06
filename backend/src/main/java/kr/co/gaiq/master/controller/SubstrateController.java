package kr.co.gaiq.master.controller;

import jakarta.validation.Valid;
import kr.co.gaiq.auth.principal.CurrentUserProvider;
import kr.co.gaiq.common.dto.PageResponse;
import kr.co.gaiq.master.dto.SubstrateDto;
import kr.co.gaiq.master.dto.SubstrateUpsertRequest;
import kr.co.gaiq.master.service.MasterDataService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/substrates")
public class SubstrateController {

    private final MasterDataService masterDataService;
    private final CurrentUserProvider currentUserProvider;

    public SubstrateController(MasterDataService masterDataService, CurrentUserProvider currentUserProvider) {
        this.masterDataService = masterDataService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping
    public ResponseEntity<PageResponse<SubstrateDto>> list(
            @RequestParam(required = false) String substrateType, Pageable pageable) {
        return ResponseEntity.ok(masterDataService.listSubstrates(substrateType, pageable));
    }

    @PostMapping
    public ResponseEntity<SubstrateDto> create(@Valid @RequestBody SubstrateUpsertRequest request) {
        Long actorUserId = currentUserProvider.getOrThrow().userId();
        return ResponseEntity.status(HttpStatus.CREATED).body(masterDataService.createSubstrate(request, actorUserId));
    }
}
