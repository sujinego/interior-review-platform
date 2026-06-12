package com.portfolio.interior.domain.post.repository;

import com.portfolio.interior.domain.post.dto.PostSearchCondition;
import com.portfolio.interior.domain.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostQueryRepository {

    Page<Post> search(PostSearchCondition condition, Pageable pageable);
}
