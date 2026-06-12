package com.portfolio.interior.domain.like.controller;

import com.portfolio.interior.domain.like.service.LikeService;
import com.portfolio.interior.global.common.ApiResponse;
import com.portfolio.interior.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts/{postId}/like")
public class LikeController {

    private final LikeService likeService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ApiResponse<Void> like(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long postId) {
        likeService.like(userDetails.getId(), postId);
        return ApiResponse.success();
    }

    @DeleteMapping
    public ApiResponse<Void> unlike(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long postId) {
        likeService.unlike(userDetails.getId(), postId);
        return ApiResponse.success();
    }
}
