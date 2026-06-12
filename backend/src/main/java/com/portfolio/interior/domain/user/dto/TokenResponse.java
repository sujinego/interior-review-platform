package com.portfolio.interior.domain.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TokenResponse {

    private final String accessToken;
    private final String tokenType;
    private final long expiresIn;
}
