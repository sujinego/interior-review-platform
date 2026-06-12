package com.portfolio.interior.domain.user.service;

import com.portfolio.interior.domain.user.dto.LoginRequest;
import com.portfolio.interior.domain.user.dto.SignupRequest;
import com.portfolio.interior.domain.user.dto.TokenResponse;
import com.portfolio.interior.domain.user.entity.User;
import com.portfolio.interior.domain.user.repository.UserRepository;
import com.portfolio.interior.global.exception.CustomException;
import com.portfolio.interior.global.exception.ErrorCode;
import com.portfolio.interior.global.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public Long signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }
        if (userRepository.existsByNickname(request.getNickname())) {
            throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
        }

        User user = request.toEntity(passwordEncoder);
        return userRepository.save(user).getId();
    }

    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.LOGIN_FAILED));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.LOGIN_FAILED);
        }

        return jwtTokenProvider.generateToken(user);
    }
}
