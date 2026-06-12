package com.portfolio.interior.domain.comment.dto;

import com.portfolio.interior.domain.comment.entity.Comment;
import com.portfolio.interior.domain.post.entity.Post;
import com.portfolio.interior.domain.user.entity.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CommentCreateRequest {

    @NotBlank
    @Size(max = 2000)
    private String content;

    private Long parentId;

    public Comment toEntity(Post post, User user) {
        return Comment.builder()
                .post(post)
                .user(user)
                .parentId(parentId)
                .content(content)
                .build();
    }
}
