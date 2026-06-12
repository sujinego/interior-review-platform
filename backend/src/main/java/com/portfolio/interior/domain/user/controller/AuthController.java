package com.portfolio.interior.domain.user.controller;

import com.portfolio.interior.domain.user.dto.LoginRequest;
import com.portfolio.interior.domain.user.dto.SignupRequest;
import com.portfolio.interior.domain.user.dto.TokenResponse;
import com.portfolio.interior.domain.user.dto.UserResponse;
import com.portfolio.interior.domain.user.service.AuthService;
import com.portfolio.interior.domain.user.service.UserService;
import com.portfolio.interior.global.common.ApiResponse;
import com.portfolio.interior.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/signup")
    public ApiResponse<Long> signup(@Valid @RequestBody SignupRequest request) {
        return ApiResponse.success(authService.signup(request));
    }

    @PostMapping("/login")
    public ApiResponse<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }

    @GetMapping("/me")
    public ApiResponse<UserResponse> getMyInfo(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResponse.success(userService.getMyInfo(userDetails.getId()));
    }
}
