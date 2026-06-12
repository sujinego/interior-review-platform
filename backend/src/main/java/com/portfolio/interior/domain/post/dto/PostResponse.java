package com.portfolio.interior.domain.post.dto;

import com.portfolio.interior.domain.post.entity.Post;
import com.portfolio.interior.domain.post.entity.PostCategory;
import com.portfolio.interior.domain.post.entity.PostImage;
import com.portfolio.interior.domain.post.entity.PostStyleTag;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PostResponse {

    private Long id;
    private PostCategory category;
    private String title;
    private String regionCity;
    private String regionDistrict;
    private BigDecimal areaPyeong;
    private Long totalCost;
    private Long costPerPyeong;
    private List<String> styleTags;
    private String thumbnailUrl;
    private int viewCount;
    private int likeCount;
    private int bookmarkCount;
    private LocalDateTime createdAt;

    public static PostResponse from(Post post) {
        return PostResponse.builder()
                .id(post.getId())
                .category(post.getCategory())
                .title(post.getTitle())
                .regionCity(post.getRegionCity())
                .regionDistrict(post.getRegionDistrict())
                .areaPyeong(post.getAreaPyeong())
                .totalCost(post.getTotalCost())
                .costPerPyeong(post.getCostPerPyeong())
                .styleTags(post.getStyleTags().stream().map(PostStyleTag::getTag).toList())
                .thumbnailUrl(resolveThumbnailUrl(post))
                .viewCount(post.getViewCount())
                .likeCount(post.getLikeCount())
                .bookmarkCount(post.getBookmarkCount())
                .createdAt(post.getCreatedAt())
                .build();
    }

    private static String resolveThumbnailUrl(Post post) {
        return post.getImages().stream()
                .filter(PostImage::isCover)
                .findFirst()
                .or(() -> post.getImages().stream().min(Comparator.comparingInt(PostImage::getSortOrder)))
                .map(PostImage::getUrl)
                .orElse(null);
    }
}
