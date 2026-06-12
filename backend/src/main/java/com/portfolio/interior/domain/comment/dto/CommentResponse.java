package com.portfolio.interior.domain.comment.dto;

import com.portfolio.interior.domain.comment.entity.Comment;
import com.portfolio.interior.domain.comment.entity.CommentStatus;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CommentResponse {

    private Long id;
    private Long postId;
    private Long authorId;
    private String authorNickname;
    private String authorAvatarUrl;
    private Long parentId;
    private String content;
    private CommentStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<CommentResponse> children;

    public static CommentResponse from(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .postId(comment.getPost().getId())
                .authorId(comment.getUser().getId())
                .authorNickname(comment.getUser().getNickname())
                .authorAvatarUrl(comment.getUser().getAvatarUrl())
                .parentId(comment.getParentId())
                .content(comment.getContent())
                .status(comment.getStatus())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .children(new ArrayList<>())
                .build();
    }
}
