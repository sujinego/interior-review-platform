package com.portfolio.interior.domain.post.controller;

import com.portfolio.interior.domain.post.dto.PostCreateRequest;
import com.portfolio.interior.domain.post.dto.PostDetailResponse;
import com.portfolio.interior.domain.post.dto.PostResponse;
import com.portfolio.interior.domain.post.dto.PostSearchCondition;
import com.portfolio.interior.domain.post.service.PostService;
import com.portfolio.interior.global.common.ApiResponse;
import com.portfolio.interior.global.common.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    @GetMapping("/search")
    public ApiResponse<PageResponse<PostResponse>> search(
            @ModelAttribute PostSearchCondition condition,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PostResponse> result = postService.searchPosts(condition, pageable);
        return ApiResponse.success(PageResponse.from(result));
    }

    @GetMapping("/{id}")
    public ApiResponse<PostDetailResponse> getPost(@PathVariable Long id) {
        return ApiResponse.success(postService.getPostDetail(id));
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ApiResponse<Long> createPost(@RequestParam Long userId, @Valid @RequestBody PostCreateRequest request) {
        return ApiResponse.success(postService.createPost(userId, request));
    }
}
