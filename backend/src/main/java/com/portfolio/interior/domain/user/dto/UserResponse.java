package com.portfolio.interior.domain.user.dto;

import com.portfolio.interior.domain.user.entity.User;
import com.portfolio.interior.domain.user.entity.UserRole;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponse {

    private final Long id;
    private final String email;
    private final String nickname;
    private final String avatarUrl;
    private final UserRole role;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole())
                .build();
    }
}
