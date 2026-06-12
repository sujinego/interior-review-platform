package com.portfolio.interior.domain.post.repository;

import static com.portfolio.interior.domain.post.entity.QPost.post;
import static com.portfolio.interior.domain.post.entity.QPostStyleTag.postStyleTag;

import com.portfolio.interior.domain.post.dto.PostSearchCondition;
import com.portfolio.interior.domain.post.dto.PostSortType;
import com.portfolio.interior.domain.post.entity.Post;
import com.portfolio.interior.domain.post.entity.PostCategory;
import com.portfolio.interior.domain.post.entity.PostStatus;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
@RequiredArgsConstructor
public class PostQueryRepositoryImpl implements PostQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Post> search(PostSearchCondition condition, Pageable pageable) {
        BooleanBuilder builder = buildSearchCondition(condition);

        List<Post> content = queryFactory
                .selectFrom(post)
                .where(builder)
                .orderBy(orderSpecifiers(condition.getSortOrDefault()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(post.count())
                .from(post)
                .where(builder);

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanBuilder buildSearchCondition(PostSearchCondition condition) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(statusActive());
        builder.and(categoryEq(condition.getCategory()));
        builder.and(regionCityEq(condition.getRegionCity()));
        builder.and(regionDistrictEq(condition.getRegionDistrict()));
        builder.and(areaPyeongGoe(condition.getAreaPyeongMin()));
        builder.and(areaPyeongLoe(condition.getAreaPyeongMax()));
        builder.and(totalCostGoe(condition.getTotalCostMin()));
        builder.and(totalCostLoe(condition.getTotalCostMax()));
        builder.and(styleTagsIn(condition.getStyleTags()));
        builder.and(keywordContains(condition.getKeyword()));
        return builder;
    }

    private BooleanExpression statusActive() {
        return post.status.eq(PostStatus.ACTIVE);
    }

    private BooleanExpression categoryEq(PostCategory category) {
        return category != null ? post.category.eq(category) : null;
    }

    private BooleanExpression regionCityEq(String regionCity) {
        return StringUtils.hasText(regionCity) ? post.regionCity.eq(regionCity) : null;
    }

    private BooleanExpression regionDistrictEq(String regionDistrict) {
        return StringUtils.hasText(regionDistrict) ? post.regionDistrict.eq(regionDistrict) : null;
    }

    private BooleanExpression areaPyeongGoe(Double areaPyeongMin) {
        return areaPyeongMin != null ? post.areaPyeong.goe(BigDecimal.valueOf(areaPyeongMin)) : null;
    }

    private BooleanExpression areaPyeongLoe(Double areaPyeongMax) {
        return areaPyeongMax != null ? post.areaPyeong.loe(BigDecimal.valueOf(areaPyeongMax)) : null;
    }

    private BooleanExpression totalCostGoe(Long totalCostMin) {
        return totalCostMin != null ? post.totalCost.goe(totalCostMin) : null;
    }

    private BooleanExpression totalCostLoe(Long totalCostMax) {
        return totalCostMax != null ? post.totalCost.loe(totalCostMax) : null;
    }

    /** styleTags 중 하나라도 일치하는 게시글 (OR 조건, 서브쿼리 exists 대용) */
    private BooleanExpression styleTagsIn(List<String> styleTags) {
        if (styleTags == null || styleTags.isEmpty()) {
            return null;
        }
        return post.id.in(
                JPAExpressions.select(postStyleTag.post.id)
                        .from(postStyleTag)
                        .where(postStyleTag.tag.in(styleTags))
        );
    }

    private BooleanExpression keywordContains(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }
        return post.title.containsIgnoreCase(keyword)
                .or(post.content.containsIgnoreCase(keyword));
    }

    private OrderSpecifier<?>[] orderSpecifiers(PostSortType sort) {
        return switch (sort) {
            case LIKES -> new OrderSpecifier<?>[]{post.likeCount.desc(), post.id.desc()};
            case COST_ASC -> new OrderSpecifier<?>[]{post.totalCost.asc(), post.id.desc()};
            case COST_DESC -> new OrderSpecifier<?>[]{post.totalCost.desc(), post.id.desc()};
            case LATEST -> new OrderSpecifier<?>[]{post.createdAt.desc(), post.id.desc()};
        };
    }
}
