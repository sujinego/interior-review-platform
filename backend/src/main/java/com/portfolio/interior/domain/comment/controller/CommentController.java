package com.portfolio.interior.domain.comment.controller;

import com.portfolio.interior.domain.comment.dto.CommentCreateRequest;
import com.portfolio.interior.domain.comment.dto.CommentResponse;
import com.portfolio.interior.domain.comment.dto.CommentUpdateRequest;
import com.portfolio.interior.domain.comment.service.CommentService;
import com.portfolio.interior.global.common.ApiResponse;
import com.portfolio.interior.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/api/posts/{postId}/comments")
    public ApiResponse<Long> createComment(@AuthenticationPrincipal CustomUserDetails userDetails,
                                            @PathVariable Long postId,
                                            @Valid @RequestBody CommentCreateRequest request) {
        return ApiResponse.success(commentService.createComment(userDetails.getId(), postId, request));
    }

    @GetMapping("/api/posts/{postId}/comments")
    public ApiResponse<List<CommentResponse>> getComments(@PathVariable Long postId) {
        return ApiResponse.success(commentService.getComments(postId));
    }

    @PutMapping("/api/comments/{id}")
    public ApiResponse<Void> updateComment(@AuthenticationPrincipal CustomUserDetails userDetails,
                                            @PathVariable Long id,
                                            @Valid @RequestBody CommentUpdateRequest request) {
        commentService.updateComment(userDetails.getId(), id, request);
        return ApiResponse.success();
    }

    @DeleteMapping("/api/comments/{id}")
    public ApiResponse<Void> deleteComment(@AuthenticationPrincipal CustomUserDetails userDetails,
                                            @PathVariable Long id) {
        commentService.deleteComment(userDetails.getId(), id);
        return ApiResponse.success();
    }
}
