package com.portfolio.interior.domain.like.repository;

import com.portfolio.interior.domain.like.entity.Like;
import com.portfolio.interior.domain.like.entity.LikeId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeRepository extends JpaRepository<Like, LikeId> {
}
