package com.portfolio.interior.domain.post.entity;

import com.portfolio.interior.domain.user.entity.User;
import com.portfolio.interior.global.common.BaseTimeEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "posts", indexes = {
        @Index(name = "idx_posts_status", columnList = "status"),
        @Index(name = "idx_posts_category", columnList = "category"),
        @Index(name = "idx_posts_region_city", columnList = "region_city"),
        @Index(name = "idx_posts_created_at", columnList = "created_at")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PostCategory category;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "region_city", nullable = false, length = 50)
    private String regionCity;

    @Column(name = "region_district", length = 50)
    private String regionDistrict;

    @Column(name = "area_sqm", nullable = false, precision = 8, scale = 2)
    private BigDecimal areaSqm;

    @Column(name = "area_pyeong", precision = 8, scale = 2)
    private BigDecimal areaPyeong;

    @Column(name = "construction_days")
    private Integer constructionDays;

    @Column(name = "completion_year")
    private Integer completionYear;

    @Column(name = "company_name", length = 100)
    private String companyName;

    @Column(name = "is_company_public", nullable = false)
    private boolean companyPublic;

    @Column(name = "total_cost", nullable = false)
    private Long totalCost;

    @Column(name = "cost_per_pyeong")
    private Long costPerPyeong;

    @Column(name = "is_ad", nullable = false)
    private boolean ad;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PostStatus status;

    @Column(name = "view_count", nullable = false)
    private int viewCount;

    @Column(name = "like_count", nullable = false)
    private int likeCount;

    @Column(name = "bookmark_count", nullable = false)
    private int bookmarkCount;

    @Version
    @Column(nullable = false)
    private Long version;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CostItem> costItems = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostStyleTag> styleTags = new ArrayList<>();

    @Builder
    public Post(User user, PostCategory category, String title, String content,
                 String regionCity, String regionDistrict, BigDecimal areaSqm, BigDecimal areaPyeong,
                 Integer constructionDays, Integer completionYear, String companyName, Boolean companyPublic,
                 Long totalCost, Long costPerPyeong, Boolean ad) {
        this.user = user;
        this.category = category;
        this.title = title;
        this.content = content;
        this.regionCity = regionCity;
        this.regionDistrict = regionDistrict;
        this.areaSqm = areaSqm;
        this.areaPyeong = areaPyeong;
        this.constructionDays = constructionDays;
        this.completionYear = completionYear;
        this.companyName = companyName;
        this.companyPublic = companyPublic == null || companyPublic;
        this.totalCost = totalCost;
        this.costPerPyeong = costPerPyeong;
        this.ad = ad != null && ad;
        this.status = PostStatus.ACTIVE;
        this.viewCount = 0;
        this.likeCount = 0;
        this.bookmarkCount = 0;
    }

    public void addCostItem(CostItem costItem) {
        this.costItems.add(costItem);
        costItem.setPost(this);
    }

    public void addImage(PostImage image) {
        this.images.add(image);
        image.setPost(this);
    }

    public void addStyleTag(PostStyleTag styleTag) {
        this.styleTags.add(styleTag);
        styleTag.setPost(this);
    }

    public void increaseViewCount() {
        this.viewCount++;
    }

    public void increaseLikeCount() {
        this.likeCount++;
    }

    public void decreaseLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }

    public void blind() {
        this.status = PostStatus.BLINDED;
    }
}
