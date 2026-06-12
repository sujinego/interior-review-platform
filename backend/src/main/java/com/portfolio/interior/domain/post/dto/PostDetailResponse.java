package com.portfolio.interior.domain.post.dto;

import com.portfolio.interior.domain.post.entity.Post;
import com.portfolio.interior.domain.post.entity.PostCategory;
import com.portfolio.interior.domain.post.entity.PostStatus;
import com.portfolio.interior.domain.post.entity.PostStyleTag;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PostDetailResponse {

    private Long id;
    private Long authorId;
    private String authorNickname;
    private PostCategory category;
    private String title;
    private String content;
    private String regionCity;
    private String regionDistrict;
    private BigDecimal areaSqm;
    private BigDecimal areaPyeong;
    private Integer constructionDays;
    private Integer completionYear;
    private String companyName;
    private boolean companyPublic;
    private Long totalCost;
    private Long costPerPyeong;
    private boolean ad;
    private PostStatus status;
    private int viewCount;
    private int likeCount;
    private int bookmarkCount;
    private List<String> styleTags;
    private List<CostItemResponse> costItems;
    private List<PostImageResponse> images;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PostDetailResponse from(Post post) {
        return PostDetailResponse.builder()
                .id(post.getId())
                .authorId(post.getUser().getId())
                .authorNickname(post.getUser().getNickname())
                .category(post.getCategory())
                .title(post.getTitle())
                .content(post.getContent())
                .regionCity(post.getRegionCity())
                .regionDistrict(post.getRegionDistrict())
                .areaSqm(post.getAreaSqm())
                .areaPyeong(post.getAreaPyeong())
                .constructionDays(post.getConstructionDays())
                .completionYear(post.getCompletionYear())
                .companyName(post.getCompanyName())
                .companyPublic(post.isCompanyPublic())
                .totalCost(post.getTotalCost())
                .costPerPyeong(post.getCostPerPyeong())
                .ad(post.isAd())
                .status(post.getStatus())
                .viewCount(post.getViewCount())
                .likeCount(post.getLikeCount())
                .bookmarkCount(post.getBookmarkCount())
                .styleTags(post.getStyleTags().stream().map(PostStyleTag::getTag).toList())
                .costItems(post.getCostItems().stream().map(CostItemResponse::from).toList())
                .images(post.getImages().stream().map(PostImageResponse::from).toList())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}
