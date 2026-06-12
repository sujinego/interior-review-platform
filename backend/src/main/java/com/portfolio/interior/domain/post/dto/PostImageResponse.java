package com.portfolio.interior.domain.post.dto;

import com.portfolio.interior.domain.post.entity.PostImage;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PostImageResponse {

    private Long id;
    private String url;
    private boolean cover;
    private int sortOrder;

    public static PostImageResponse from(PostImage image) {
        return PostImageResponse.builder()
                .id(image.getId())
                .url(image.getUrl())
                .cover(image.isCover())
                .sortOrder(image.getSortOrder())
                .build();
    }
}
