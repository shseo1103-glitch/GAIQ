package kr.co.gaiq.org.service;

import java.security.SecureRandom;
import java.util.LinkedHashMap;
import java.util.Map;
import kr.co.gaiq.audit.service.AuditRecorder;
import kr.co.gaiq.common.dto.PageResponse;
import kr.co.gaiq.common.exception.BusinessRuleViolationException;
import kr.co.gaiq.common.exception.EntityNotFoundException;
import kr.co.gaiq.org.dto.ApproveRequest;
import kr.co.gaiq.org.dto.OnboardingRequest;
import kr.co.gaiq.org.dto.OrganizationDto;
import kr.co.gaiq.org.dto.OrganizationUpsertRequest;
import kr.co.gaiq.org.dto.RejectRequest;
import kr.co.gaiq.org.entity.Organization;
import kr.co.gaiq.org.entity.UserAccount;
import kr.co.gaiq.org.repository.OrganizationRepository;
import kr.co.gaiq.org.repository.UserAccountRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Organization onboarding (ADOPTER 가입신청 → PLATFORM_ADMIN 승인) + org CRUD —
 * implements "조직-온보딩" / "조직-사용자관리" tags of {@code 01-gaiq-core.yaml}.
 */
@Service
public class OrganizationService {

    private static final String MODULE = "ORG_ADMIN";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final OrganizationRepository organizationRepository;
    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditRecorder auditRecorder;

    public OrganizationService(
            OrganizationRepository organizationRepository,
            UserAccountRepository userAccountRepository,
            PasswordEncoder passwordEncoder,
            AuditRecorder auditRecorder) {
        this.organizationRepository = organizationRepository;
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditRecorder = auditRecorder;
    }

    @Transactional
    public OrganizationDto submitOnboardingRequest(OnboardingRequest request) {
        Organization org = new Organization();
        org.setOrgCode(generateOrgCode());
        org.setOrgType("ADOPTER");
        org.setOrgName(request.orgName());
        org.setIndustryType(request.industryType());
        org.setBusinessRegNo(request.businessRegNo());
        org.setStatus("PENDING");
        org.setContactName(request.contactName());
        org.setContactEmail(request.contactEmail());
        org.setContactPhone(request.contactPhone());
        organizationRepository.save(org);

        auditRecorder.record(
                org.getOrgId(), null, "SYSTEM", MODULE, "CREATE", "Organization", org.getOrgId(),
                null, snapshot(org));

        return OrganizationDto.from(org);
    }

    @Transactional(readOnly = true)
    public PageResponse<OrganizationDto> list(String status, String orgType, Pageable pageable) {
        Page<Organization> page;
        if (status != null && orgType != null) {
            page = organizationRepository.findByStatusAndOrgType(status, orgType, pageable);
        } else if (status != null) {
            page = organizationRepository.findByStatus(status, pageable);
        } else if (orgType != null) {
            page = organizationRepository.findByOrgType(orgType, pageable);
        } else {
            page = organizationRepository.findAll(pageable);
        }
        return PageResponse.of(page, OrganizationDto::from);
    }

    @Transactional(readOnly = true)
    public OrganizationDto get(Long orgId) {
        return OrganizationDto.from(getOrThrow(orgId));
    }

    @Transactional
    public OrganizationDto update(Long orgId, OrganizationUpsertRequest request, Long actorUserId) {
        Organization org = getOrThrow(orgId);
        Map<String, Object> before = snapshot(org);

        if (request.orgName() != null) {
            org.setOrgName(request.orgName());
        }
        if (request.industryType() != null) {
            org.setIndustryType(request.industryType());
        }
        if (request.status() != null) {
            org.setStatus(request.status());
        }
        if (request.contactName() != null) {
            org.setContactName(request.contactName());
        }
        if (request.contactEmail() != null) {
            org.setContactEmail(request.contactEmail());
        }
        if (request.contactPhone() != null) {
            org.setContactPhone(request.contactPhone());
        }

        auditRecorder.recordUserAction(
                orgId, actorUserId, MODULE, "UPDATE", "Organization", orgId, before, snapshot(org));

        return OrganizationDto.from(org);
    }

    @Transactional
    public OrganizationDto approve(Long orgId, ApproveRequest request, Long actorUserId) {
        Organization org = getOrThrow(orgId);
        if (!"PENDING".equals(org.getStatus())) {
            throw new BusinessRuleViolationException("ORG_NOT_PENDING", "PENDING 상태의 조직만 승인할 수 있습니다.");
        }
        Map<String, Object> before = snapshot(org);

        org.setStatus("ACTIVE");
        org.setApprovedBy(actorUserId);
        org.setApprovedAt(java.time.Instant.now());

        String loginId = request.initialAdminLoginId() != null
                ? request.initialAdminLoginId()
                : "admin_" + org.getOrgCode().toLowerCase();
        String tempPassword = generateTempPassword();

        UserAccount admin = new UserAccount();
        admin.setOrgId(org.getOrgId());
        admin.setLoginId(loginId);
        admin.setPasswordHash(passwordEncoder.encode(tempPassword));
        admin.setRole("PROCESS_ENGINEER");
        admin.setName(org.getContactName() != null ? org.getContactName() : org.getOrgName() + " 관리자");
        admin.setEmail(request.initialAdminEmail() != null ? request.initialAdminEmail() : org.getContactEmail());
        admin.setStatus("ACTIVE");
        userAccountRepository.save(admin);

        auditRecorder.recordUserAction(
                orgId, actorUserId, MODULE, "UPDATE", "Organization", orgId, before, snapshot(org));

        return OrganizationDto.from(org);
    }

    @Transactional
    public OrganizationDto reject(Long orgId, RejectRequest request, Long actorUserId) {
        Organization org = getOrThrow(orgId);
        if (!"PENDING".equals(org.getStatus())) {
            throw new BusinessRuleViolationException("ORG_NOT_PENDING", "PENDING 상태의 조직만 반려할 수 있습니다.");
        }
        Map<String, Object> before = snapshot(org);

        org.setStatus("SUSPENDED");

        Map<String, Object> after = snapshot(org);
        after.put("rejectReason", request.reason());

        auditRecorder.recordUserAction(
                orgId, actorUserId, MODULE, "UPDATE", "Organization", orgId, before, after);

        return OrganizationDto.from(org);
    }

    private Organization getOrThrow(Long orgId) {
        return organizationRepository.findById(orgId)
                .orElseThrow(() -> new EntityNotFoundException("Organization", orgId));
    }

    private String generateOrgCode() {
        String code;
        do {
            code = "ADP" + (100000 + RANDOM.nextInt(900000));
        } while (organizationRepository.existsByOrgCode(code));
        return code;
    }

    private String generateTempPassword() {
        return "Temp" + (100000 + RANDOM.nextInt(900000)) + "!";
    }

    private Map<String, Object> snapshot(Organization o) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("orgId", o.getOrgId());
        m.put("orgCode", o.getOrgCode());
        m.put("orgType", o.getOrgType());
        m.put("orgName", o.getOrgName());
        m.put("status", o.getStatus());
        m.put("industryType", o.getIndustryType());
        return m;
    }
}
