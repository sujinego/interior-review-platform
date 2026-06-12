package com.portfolio.interior.domain.comment.service;

import com.portfolio.interior.domain.comment.dto.CommentCreateRequest;
import com.portfolio.interior.domain.comment.dto.CommentResponse;
import com.portfolio.interior.domain.comment.dto.CommentUpdateRequest;
import com.portfolio.interior.domain.comment.entity.Comment;
import com.portfolio.interior.domain.comment.repository.CommentRepository;
import com.portfolio.interior.domain.post.entity.Post;
import com.portfolio.interior.domain.post.entity.PostStatus;
import com.portfolio.interior.domain.post.repository.PostRepository;
import com.portfolio.interior.domain.user.entity.User;
import com.portfolio.interior.domain.user.repository.UserRepository;
import com.portfolio.interior.global.exception.CustomException;
import com.portfolio.interior.global.exception.ErrorCode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional
    public Long createComment(Long userId, Long postId, CommentCreateRequest request) {
        Post post = postRepository.findById(postId)
                .filter(p -> p.getStatus() != PostStatus.DELETED)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (request.getParentId() != null) {
            Comment parent = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));
            if (!parent.getPost().getId().equals(postId)) {
                throw new CustomException(ErrorCode.COMMENT_NOT_FOUND);
            }
        }

        Comment comment = request.toEntity(post, user);
        return commentRepository.save(comment).getId();
    }

    public List<CommentResponse> getComments(Long postId) {
        List<Comment> comments = commentRepository.findAllByPostIdOrderByIdAsc(postId);

        Map<Long, CommentResponse> responseById = new LinkedHashMap<>();
        List<CommentResponse> roots = new ArrayList<>();

        for (Comment comment : comments) {
            CommentResponse response = CommentResponse.from(comment);
            responseById.put(comment.getId(), response);

            CommentResponse parent = comment.getParentId() == null ? null : responseById.get(comment.getParentId());
            if (parent != null) {
                parent.getChildren().add(response);
            } else {
                roots.add(response);
            }
        }

        return roots;
    }

    @Transactional
    public void updateComment(Long userId, Long commentId, CommentUpdateRequest request) {
        Comment comment = getCommentOwnedBy(userId, commentId);
        comment.updateContent(request.getContent());
    }

    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        Comment comment = getCommentOwnedBy(userId, commentId);
        comment.delete();
    }

    private Comment getCommentOwnedBy(Long userId, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

        if (!comment.isOwnedBy(userId)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
        return comment;
    }
}
