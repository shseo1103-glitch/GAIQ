package kr.co.gaiq.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Issues/validates JWT access/refresh tokens (jjwt 0.12.x builder API, HS256).
 * Claims carry userId/orgId/role so requests can be scoped without extra DB hits per request
 * (the service layer still re-validates org/status where needed).
 */
@Component
public class JwtTokenProvider {

    private static final String CLAIM_USER_ID = "userId";
    private static final String CLAIM_ORG_ID = "orgId";
    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_TOKEN_TYPE = "tokenType";

    public static final String TOKEN_TYPE_ACCESS = "ACCESS";
    public static final String TOKEN_TYPE_REFRESH = "REFRESH";

    private final SecretKey key;
    private final long accessTokenValidityMs;
    private final long refreshTokenValidityMs;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-validity-ms}") long accessTokenValidityMs,
            @Value("${jwt.refresh-token-validity-ms}") long refreshTokenValidityMs) {
        byte[] rawKey = secret.getBytes(StandardCharsets.UTF_8);
        this.key = Keys.hmacShaKeyFor(normalizeKeyLength(rawKey));
        this.accessTokenValidityMs = accessTokenValidityMs;
        this.refreshTokenValidityMs = refreshTokenValidityMs;
    }

    private static byte[] normalizeKeyLength(byte[] rawKey) {
        if (rawKey.length >= 32) {
            return rawKey;
        }
        byte[] padded = new byte[32];
        for (int i = 0; i < 32; i++) {
            padded[i] = rawKey[i % rawKey.length];
        }
        return padded;
    }

    public String generateAccessToken(Long userId, Long orgId, String role) {
        return generateToken(userId, orgId, role, TOKEN_TYPE_ACCESS, accessTokenValidityMs);
    }

    public String generateRefreshToken(Long userId, Long orgId, String role) {
        return generateToken(userId, orgId, role, TOKEN_TYPE_REFRESH, refreshTokenValidityMs);
    }

    private String generateToken(Long userId, Long orgId, String role, String tokenType, long validityMs) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + validityMs);
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim(CLAIM_USER_ID, userId)
                .claim(CLAIM_ORG_ID, orgId)
                .claim(CLAIM_ROLE, role)
                .claim(CLAIM_TOKEN_TYPE, tokenType)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Long getUserId(String token) {
        return parseClaims(token).get(CLAIM_USER_ID, Long.class);
    }

    public Long getOrgId(String token) {
        return parseClaims(token).get(CLAIM_ORG_ID, Long.class);
    }

    public String getRole(String token) {
        return parseClaims(token).get(CLAIM_ROLE, String.class);
    }

    public String getTokenType(String token) {
        return parseClaims(token).get(CLAIM_TOKEN_TYPE, String.class);
    }

    public java.time.Instant getExpiration(String token) {
        return parseClaims(token).getExpiration().toInstant();
    }

    public int getAccessTokenValiditySeconds() {
        return (int) (accessTokenValidityMs / 1000);
    }

    Key key() {
        return key;
    }
}
