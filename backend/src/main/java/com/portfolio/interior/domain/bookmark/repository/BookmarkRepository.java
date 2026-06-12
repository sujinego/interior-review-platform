package com.portfolio.interior.domain.bookmark.repository;

import com.portfolio.interior.domain.bookmark.entity.Bookmark;
import com.portfolio.interior.domain.bookmark.entity.BookmarkId;
import com.portfolio.interior.domain.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookmarkRepository extends JpaRepository<Bookmark, BookmarkId> {

    @Query("SELECT b.post FROM Bookmark b WHERE b.id.userId = :userId ORDER BY b.createdAt DESC")
    Page<Post> findBookmarkedPosts(@Param("userId") Long userId, Pageable pageable);
}
