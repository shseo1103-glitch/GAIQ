package kr.co.gaiq.org.controller;

import jakarta.validation.Valid;
import kr.co.gaiq.auth.principal.CurrentUserProvider;
import kr.co.gaiq.common.dto.PageResponse;
import kr.co.gaiq.org.dto.ApproveRequest;
import kr.co.gaiq.org.dto.OnboardingRequest;
import kr.co.gaiq.org.dto.OrganizationDto;
import kr.co.gaiq.org.dto.OrganizationUpsertRequest;
import kr.co.gaiq.org.dto.RejectRequest;
import kr.co.gaiq.org.service.OrganizationService;
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

/**
 * Implements "조직-온보딩"/"조직-사용자관리" 조직 관련 경로 of {@code 01-gaiq-core.yaml}.
 */
@RestController
@RequestMapping("/api/v1")
public class OrganizationController {

    private final OrganizationService organizationService;
    private final CurrentUserProvider currentUserProvider;

    public OrganizationController(OrganizationService organizationService, CurrentUserProvider currentUserProvider) {
        this.organizationService = organizationService;
        this.currentUserProvider = currentUserProvider;
    }

    @PostMapping("/organizations/onboarding-requests")
    public ResponseEntity<OrganizationDto> submitOnboarding(@Valid @RequestBody OnboardingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(organizationService.submitOnboardingRequest(request));
    }

    @PostMapping("/organizations/{orgId}/approve")
    public ResponseEntity<OrganizationDto> approve(
            @PathVariable Long orgId, @RequestBody(required = false) ApproveRequest request) {
        ApproveRequest body = request != null ? request : new ApproveRequest(null, null);
        Long actorUserId = currentUserProvider.getOrThrow().userId();
        return ResponseEntity.ok(organizationService.approve(orgId, body, actorUserId));
    }

    @PostMapping("/organizations/{orgId}/reject")
    public ResponseEntity<OrganizationDto> reject(
            @PathVariable Long orgId, @Valid @RequestBody RejectRequest request) {
        Long actorUserId = currentUserProvider.getOrThrow().userId();
        return ResponseEntity.ok(organizationService.reject(orgId, request, actorUserId));
    }

    @GetMapping("/organizations")
    public ResponseEntity<PageResponse<OrganizationDto>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String orgType,
            Pageable pageable) {
        return ResponseEntity.ok(organizationService.list(status, orgType, pageable));
    }

    @GetMapping("/organizations/{orgId}")
    public ResponseEntity<OrganizationDto> get(@PathVariable Long orgId) {
        return ResponseEntity.ok(organizationService.get(orgId));
    }

    @PutMapping("/organizations/{orgId}")
    public ResponseEntity<OrganizationDto> update(
            @PathVariable Long orgId, @RequestBody OrganizationUpsertRequest request) {
        Long actorUserId = currentUserProvider.getOrThrow().userId();
        return ResponseEntity.ok(organizationService.update(orgId, request, actorUserId));
    }
}
