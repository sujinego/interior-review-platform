package com.portfolio.interior.domain.post.dto;

import com.portfolio.interior.domain.post.entity.Post;
import com.portfolio.interior.domain.post.entity.PostCategory;
import com.portfolio.interior.domain.post.entity.PostStyleTag;
import com.portfolio.interior.domain.user.entity.User;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PostCreateRequest {

    @NotNull
    private PostCategory category;

    @NotBlank
    @Size(max = 200)
    private String title;

    @NotBlank
    private String content;

    @NotBlank
    @Size(max = 50)
    private String regionCity;

    @Size(max = 50)
    private String regionDistrict;

    @NotNull
    @DecimalMin(value = "0", inclusive = false)
    private BigDecimal areaSqm;

    private BigDecimal areaPyeong;

    @PositiveOrZero
    private Integer constructionDays;

    private Integer completionYear;

    @Size(max = 100)
    private String companyName;

    private Boolean companyPublic;

    @NotNull
    @PositiveOrZero
    private Long totalCost;

    @PositiveOrZero
    private Long costPerPyeong;

    private Boolean ad;

    private List<String> styleTags = new ArrayList<>();

    @Valid
    private List<CostItemRequest> costItems = new ArrayList<>();

    @Valid
    private List<PostImageRequest> images = new ArrayList<>();

    public Post toEntity(User user) {
        Post post = Post.builder()
                .user(user)
                .category(category)
                .title(title)
                .content(content)
                .regionCity(regionCity)
                .regionDistrict(regionDistrict)
                .areaSqm(areaSqm)
                .areaPyeong(areaPyeong)
                .constructionDays(constructionDays)
                .completionYear(completionYear)
                .companyName(companyName)
                .companyPublic(companyPublic)
                .totalCost(totalCost)
                .costPerPyeong(costPerPyeong)
                .ad(ad)
                .build();

        styleTags.forEach(tag -> post.addStyleTag(PostStyleTag.builder().tag(tag).build()));
        costItems.forEach(item -> post.addCostItem(item.toEntity()));
        images.forEach(image -> post.addImage(image.toEntity()));

        return post;
    }
}
