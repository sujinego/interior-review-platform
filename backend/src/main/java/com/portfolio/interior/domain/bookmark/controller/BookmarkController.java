package com.portfolio.interior.domain.bookmark.controller;

import com.portfolio.interior.domain.bookmark.service.BookmarkService;
import com.portfolio.interior.domain.post.dto.PostResponse;
import com.portfolio.interior.global.common.ApiResponse;
import com.portfolio.interior.global.common.PageResponse;
import com.portfolio.interior.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/api/posts/{postId}/bookmark")
    public ApiResponse<Void> bookmark(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long postId) {
        bookmarkService.bookmark(userDetails.getId(), postId);
        return ApiResponse.success();
    }

    @DeleteMapping("/api/posts/{postId}/bookmark")
    public ApiResponse<Void> unbookmark(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long postId) {
        bookmarkService.unbookmark(userDetails.getId(), postId);
        return ApiResponse.success();
    }

    @GetMapping("/api/users/me/bookmarks")
    public ApiResponse<PageResponse<PostResponse>> getMyBookmarks(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PostResponse> result = bookmarkService.getMyBookmarks(userDetails.getId(), pageable);
        return ApiResponse.success(PageResponse.from(result));
    }
}
