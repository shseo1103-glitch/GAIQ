package kr.co.gaiq.config;

import kr.co.gaiq.auth.jwt.JwtAuthenticationFilter;
import kr.co.gaiq.auth.jwt.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Stateless JWT-based Spring Security configuration.
 *
 * <p>{@code /auth/login}, onboarding-request submission, and Swagger/Actuator health are open;
 * everything else requires a valid JWT access token, checked by {@link JwtAuthenticationFilter}.
 * org_id scoping for non-PLATFORM_ADMIN roles is enforced in the service layer, not here.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final String[] PERMIT_ALL_PATHS = {
            "/api/v1/auth/login",
            "/api/v1/organizations/onboarding-requests",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/actuator/health",
            "/actuator/info"
    };

    // 정확한 오리진 목망(localhost 등)과, 패턴 기반 오리진(임의 host + 포트 8114)을 분리 설정한다.
    // 2026-09-07 수정: 사용자가 localhost가 아니림 공개IP/도음으로 접속하자 CORS가 버부되어(403) 로그인이 안 되는 문제가 발견되어,
    // 특정 포트(8114)로 끈낞는 모닠 오리진을 허용하는 패턴을 추가함(대신 credential포함 요캭이 대상이버 패턴 도유자 대상에만 적용되는 점 유지).
    @Value("${gaiq.cors.allowed-origins:http://localhost:8114,http://127.0.0.1:8114}")
    private List<String> allowedOrigins;

    @Value("${gaiq.cors.allowed-origin-patterns:http://*:8114,https://*:8114}")
    private List<String> allowedOriginPatterns;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedOriginPatterns(allowedOriginPatterns);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtTokenProvider jwtTokenProvider)
            throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PERMIT_ALL_PATHS).permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
