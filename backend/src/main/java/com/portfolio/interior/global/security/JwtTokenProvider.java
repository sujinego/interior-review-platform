package com.portfolio.interior.global.security;

import com.portfolio.interior.domain.user.entity.User;
import com.portfolio.interior.domain.user.entity.UserRole;
import com.portfolio.interior.domain.user.dto.TokenResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
public class JwtTokenProvider {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String CLAIM_EMAIL = "email";
    private static final String CLAIM_NICKNAME = "nickname";
    private static final String CLAIM_ROLE = "role";

    private final Key key;
    private final long accessTokenExpiration;

    public JwtTokenProvider(@Value("${jwt.secret}") String secret,
                             @Value("${jwt.access-token-expiration}") long accessTokenExpiration) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiration = accessTokenExpiration;
    }

    public TokenResponse generateToken(User user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenExpiration);

        String accessToken = Jwts.builder()
                .setSubject(String.valueOf(user.getId()))
                .claim(CLAIM_EMAIL, user.getEmail())
                .claim(CLAIM_NICKNAME, user.getNickname())
                .claim(CLAIM_ROLE, user.getRole().name())
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        return TokenResponse.builder()
                .accessToken(accessToken)
                .tokenType(BEARER_PREFIX.trim())
                .expiresIn(accessTokenExpiration / 1000)
                .build();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.debug("Expired JWT token: {}", e.getMessage());
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Invalid JWT token: {}", e.getMessage());
        }
        return false;
    }

    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);

        Long userId = Long.valueOf(claims.getSubject());
        String email = claims.get(CLAIM_EMAIL, String.class);
        String nickname = claims.get(CLAIM_NICKNAME, String.class);
        UserRole role = UserRole.valueOf(claims.get(CLAIM_ROLE, String.class));

        CustomUserDetails principal = new CustomUserDetails(userId, email, nickname, role);
        return new UsernamePasswordAuthenticationToken(principal, token, principal.getAuthorities());
    }

    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }
}
