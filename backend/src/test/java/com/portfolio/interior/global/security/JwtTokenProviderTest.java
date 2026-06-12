package com.portfolio.interior.global.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.portfolio.interior.domain.user.dto.TokenResponse;
import com.portfolio.interior.domain.user.entity.User;
import com.portfolio.interior.domain.user.entity.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

class JwtTokenProviderTest {

    private static final String SECRET =
            "test-jwt-secret-key-for-unit-and-integration-tests-only-not-for-production";

    private final JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(SECRET, 3_600_000L);

    @Test
    @DisplayName("토큰을 생성하고, 생성된 토큰으로 인증 정보를 복원할 수 있다")
    void generateTokenAndRestoreAuthentication() {
        User user = createUser(1L, "test@example.com", "tester", UserRole.USER);

        TokenResponse tokenResponse = jwtTokenProvider.generateToken(user);

        assertThat(tokenResponse.getAccessToken()).isNotBlank();
        assertThat(tokenResponse.getTokenType()).isEqualTo("Bearer");
        assertThat(tokenResponse.getExpiresIn()).isEqualTo(3600);
        assertThat(jwtTokenProvider.validateToken(tokenResponse.getAccessToken())).isTrue();

        Authentication authentication = jwtTokenProvider.getAuthentication(tokenResponse.getAccessToken());
        CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();

        assertThat(principal.getId()).isEqualTo(1L);
        assertThat(principal.getEmail()).isEqualTo("test@example.com");
        assertThat(principal.getNickname()).isEqualTo("tester");
        assertThat(principal.getAuthorities())
                .extracting(Object::toString)
                .containsExactly("ROLE_USER");
    }

    @Test
    @DisplayName("형식이 올바르지 않은 토큰은 검증에 실패한다")
    void invalidTokenFailsValidation() {
        assertThat(jwtTokenProvider.validateToken("invalid-token")).isFalse();
    }

    @Test
    @DisplayName("만료된 토큰은 검증에 실패한다")
    void expiredTokenFailsValidation() {
        JwtTokenProvider expiredTokenProvider = new JwtTokenProvider(SECRET, -1_000L);
        User user = createUser(1L, "test@example.com", "tester", UserRole.USER);

        TokenResponse tokenResponse = expiredTokenProvider.generateToken(user);

        assertThat(jwtTokenProvider.validateToken(tokenResponse.getAccessToken())).isFalse();
    }

    private User createUser(Long id, String email, String nickname, UserRole role) {
        User user = User.builder()
                .email(email)
                .password("encoded-password")
                .nickname(nickname)
                .role(role)
                .active(true)
                .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}
