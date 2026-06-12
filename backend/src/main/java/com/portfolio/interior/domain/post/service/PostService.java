package com.portfolio.interior.domain.post.service;

import com.portfolio.interior.domain.post.dto.PostCreateRequest;
import com.portfolio.interior.domain.post.dto.PostDetailResponse;
import com.portfolio.interior.domain.post.dto.PostResponse;
import com.portfolio.interior.domain.post.dto.PostSearchCondition;
import com.portfolio.interior.domain.post.entity.Post;
import com.portfolio.interior.domain.post.entity.PostStatus;
import com.portfolio.interior.domain.post.repository.PostRepository;
import com.portfolio.interior.domain.user.entity.User;
import com.portfolio.interior.domain.user.repository.UserRepository;
import com.portfolio.interior.global.exception.CustomException;
import com.portfolio.interior.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional
    public Long createPost(Long userId, PostCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Post post = request.toEntity(user);
        return postRepository.save(post).getId();
    }

    public PostDetailResponse getPostDetail(Long postId) {
        Post post = postRepository.findById(postId)
                .filter(p -> p.getStatus() != PostStatus.DELETED)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        return PostDetailResponse.from(post);
    }

    public Page<PostResponse> searchPosts(PostSearchCondition condition, Pageable pageable) {
        return postRepository.search(condition, pageable)
                .map(PostResponse::from);
    }
}
