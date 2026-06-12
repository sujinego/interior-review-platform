package com.portfolio.interior.domain.user.service;

import com.portfolio.interior.domain.user.dto.UserResponse;
import com.portfolio.interior.domain.user.entity.User;
import com.portfolio.interior.domain.user.repository.UserRepository;
import com.portfolio.interior.global.exception.CustomException;
import com.portfolio.interior.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public UserResponse getMyInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return UserResponse.from(user);
    }
}
