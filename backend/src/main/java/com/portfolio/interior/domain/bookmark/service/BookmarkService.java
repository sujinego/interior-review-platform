package com.portfolio.interior.domain.bookmark.service;

import com.portfolio.interior.domain.bookmark.entity.Bookmark;
import com.portfolio.interior.domain.bookmark.entity.BookmarkId;
import com.portfolio.interior.domain.bookmark.repository.BookmarkRepository;
import com.portfolio.interior.domain.post.dto.PostResponse;
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
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional
    public void bookmark(Long userId, Long postId) {
        Post post = postRepository.findById(postId)
                .filter(p -> p.getStatus() != PostStatus.DELETED)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        BookmarkId bookmarkId = new BookmarkId(userId, postId);
        if (bookmarkRepository.existsById(bookmarkId)) {
            throw new CustomException(ErrorCode.ALREADY_BOOKMARKED);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        bookmarkRepository.save(Bookmark.builder().user(user).post(post).build());
        post.increaseBookmarkCount();
    }

    @Transactional
    public void unbookmark(Long userId, Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        BookmarkId bookmarkId = new BookmarkId(userId, postId);
        Bookmark bookmark = bookmarkRepository.findById(bookmarkId)
                .orElseThrow(() -> new CustomException(ErrorCode.BOOKMARK_NOT_FOUND));

        bookmarkRepository.delete(bookmark);
        post.decreaseBookmarkCount();
    }

    public Page<PostResponse> getMyBookmarks(Long userId, Pageable pageable) {
        return bookmarkRepository.findBookmarkedPosts(userId, pageable)
                .map(PostResponse::from);
    }
}
