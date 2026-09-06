package kr.co.gaiq.auth.service;

import kr.co.gaiq.auth.dto.LoginRequest;
import kr.co.gaiq.auth.dto.LoginResponse;
import kr.co.gaiq.auth.dto.UserAccountDto;
import kr.co.gaiq.auth.jwt.JwtTokenProvider;
import kr.co.gaiq.audit.service.AuditRecorder;
import kr.co.gaiq.common.exception.BusinessRuleViolationException;
import kr.co.gaiq.common.exception.EntityNotFoundException;
import kr.co.gaiq.org.entity.Organization;
import kr.co.gaiq.org.entity.UserAccount;
import kr.co.gaiq.org.repository.OrganizationRepository;
import kr.co.gaiq.org.repository.UserAccountRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Login/me — implements the "인증" tag of {@code 01-gaiq-core.yaml}.
 */
@Service
public class AuthService {

    private final OrganizationRepository organizationRepository;
    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuditRecorder auditRecorder;

    public AuthService(
            OrganizationRepository organizationRepository,
            UserAccountRepository userAccountRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider,
            AuditRecorder auditRecorder) {
        this.organizationRepository = organizationRepository;
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.auditRecorder = auditRecorder;
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        Organization org = organizationRepository.findByOrgCode(request.orgCode())
                .orElseThrow(() -> new BadCredentialsException("아이디 또는 비밀번호가 올바르지 않습니다."));

        UserAccount user = userAccountRepository.findByOrgIdAndLoginId(org.getOrgId(), request.loginId())
                .orElseThrow(() -> new BadCredentialsException("아이디 또는 비밀번호가 올바르지 않습니다."));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }
        if ("SUSPENDED".equals(user.getStatus())) {
            throw new BusinessRuleViolationException("ACCOUNT_SUSPENDED", "계정이 정지되었습니다.");
        }
        // PLATFORM_ADMIN(ADMIN org)은 조직 status와 무관하게 로그인 가능; 일반 조직은 ACTIVE여야 함.
        if (!"ADMIN".equals(org.getOrgType()) && !"ACTIVE".equals(org.getStatus())) {
            throw new BusinessRuleViolationException(
                    "ORG_NOT_ACTIVE", "조직이 ACTIVE 상태가 아닙니다(현재: " + org.getStatus() + ").");
        }

        user.recordLogin();

        String accessToken = jwtTokenProvider.generateAccessToken(user.getUserId(), user.getOrgId(), user.getRole());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUserId(), user.getOrgId(), user.getRole());

        auditRecorder.recordUserAction(
                user.getOrgId(), user.getUserId(), "AUTH", "LOGIN", "UserAccount", user.getUserId(), null, null);

        return new LoginResponse(accessToken, refreshToken, UserAccountDto.from(user));
    }

    @Transactional(readOnly = true)
    public UserAccountDto me(Long userId) {
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("UserAccount", userId));
        return UserAccountDto.from(user);
    }

    public void logout(Long userId, Long orgId) {
        if (userId != null) {
            auditRecorder.recordUserAction(orgId, userId, "AUTH", "LOGOUT", "UserAccount", userId, null, null);
        }
    }
}
