package com.portfolio.interior.domain.like.service;

import com.portfolio.interior.domain.like.entity.Like;
import com.portfolio.interior.domain.like.entity.LikeId;
import com.portfolio.interior.domain.like.repository.LikeRepository;
import com.portfolio.interior.domain.post.entity.Post;
import com.portfolio.interior.domain.post.entity.PostStatus;
import com.portfolio.interior.domain.post.repository.PostRepository;
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
public class LikeService {

    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional
    public void like(Long userId, Long postId) {
        Post post = postRepository.findById(postId)
                .filter(p -> p.getStatus() != PostStatus.DELETED)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        LikeId likeId = new LikeId(userId, postId);
        if (likeRepository.existsById(likeId)) {
            throw new CustomException(ErrorCode.ALREADY_LIKED);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        likeRepository.save(Like.builder().user(user).post(post).build());
        post.increaseLikeCount();
    }

    @Transactional
    public void unlike(Long userId, Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        LikeId likeId = new LikeId(userId, postId);
        Like like = likeRepository.findById(likeId)
                .orElseThrow(() -> new CustomException(ErrorCode.LIKE_NOT_FOUND));

        likeRepository.delete(like);
        post.decreaseLikeCount();
    }
}
