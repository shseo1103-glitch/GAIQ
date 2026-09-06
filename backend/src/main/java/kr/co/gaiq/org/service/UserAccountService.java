package kr.co.gaiq.org.service;

import java.security.SecureRandom;
import java.util.LinkedHashMap;
import java.util.Map;
import kr.co.gaiq.audit.service.AuditRecorder;
import kr.co.gaiq.auth.dto.UserAccountDto;
import kr.co.gaiq.common.dto.PageResponse;
import kr.co.gaiq.common.exception.DuplicateResourceException;
import kr.co.gaiq.common.exception.EntityNotFoundException;
import kr.co.gaiq.org.dto.UserAccountCreateRequest;
import kr.co.gaiq.org.dto.UserAccountUpdateRequest;
import kr.co.gaiq.org.entity.UserAccount;
import kr.co.gaiq.org.repository.UserAccountRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Org-scoped user account CRUD — implements "조직-사용자관리" tag of {@code 01-gaiq-core.yaml}.
 */
@Service
public class UserAccountService {

    private static final String MODULE = "ORG_ADMIN";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditRecorder auditRecorder;

    public UserAccountService(
            UserAccountRepository userAccountRepository,
            PasswordEncoder passwordEncoder,
            AuditRecorder auditRecorder) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditRecorder = auditRecorder;
    }

    @Transactional(readOnly = true)
    public PageResponse<UserAccountDto> listByOrg(Long orgId, Pageable pageable) {
        return PageResponse.of(userAccountRepository.findByOrgId(orgId, pageable), UserAccountDto::from);
    }

    @Transactional
    public UserAccountDto create(Long orgId, UserAccountCreateRequest request, Long actorUserId) {
        if (userAccountRepository.existsByLoginId(request.loginId())) {
            throw new DuplicateResourceException("이미 사용 중인 로그인ID입니다: " + request.loginId());
        }

        UserAccount user = new UserAccount();
        user.setOrgId(orgId);
        user.setLoginId(request.loginId());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(request.role());
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPhone(request.phone());
        user.setStatus("ACTIVE");
        userAccountRepository.save(user);

        auditRecorder.recordUserAction(
                orgId, actorUserId, MODULE, "CREATE", "UserAccount", user.getUserId(), null, snapshot(user));

        return UserAccountDto.from(user);
    }

    @Transactional
    public UserAccountDto update(Long userId, UserAccountUpdateRequest request, Long actorUserId) {
        UserAccount user = getOrThrow(userId);
        Map<String, Object> before = snapshot(user);

        if (request.role() != null) {
            user.setRole(request.role());
        }
        if (request.name() != null) {
            user.setName(request.name());
        }
        if (request.email() != null) {
            user.setEmail(request.email());
        }
        if (request.phone() != null) {
            user.setPhone(request.phone());
        }
        if (request.status() != null) {
            user.setStatus(request.status());
        }

        auditRecorder.recordUserAction(
                user.getOrgId(), actorUserId, MODULE, "UPDATE", "UserAccount", userId, before, snapshot(user));

        return UserAccountDto.from(user);
    }

    @Transactional
    public boolean resetPassword(Long userId, Long actorUserId) {
        UserAccount user = getOrThrow(userId);
        String tempPassword = generateTempPassword();
        user.setPasswordHash(passwordEncoder.encode(tempPassword));

        auditRecorder.recordUserAction(
                user.getOrgId(), actorUserId, MODULE, "UPDATE", "UserAccount", userId,
                Map.of("action", "password_reset"), Map.of("action", "password_reset_done"));

        return true;
    }

    private UserAccount getOrThrow(Long userId) {
        return userAccountRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("UserAccount", userId));
    }

    private String generateTempPassword() {
        return "Temp" + (100000 + RANDOM.nextInt(900000)) + "!";
    }

    private Map<String, Object> snapshot(UserAccount u) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("userId", u.getUserId());
        m.put("loginId", u.getLoginId());
        m.put("role", u.getRole());
        m.put("name", u.getName());
        m.put("status", u.getStatus());
        return m;
    }
}
