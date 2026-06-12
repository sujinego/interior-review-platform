package com.portfolio.interior.domain.post.dto;

import com.portfolio.interior.domain.post.entity.PostImage;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PostImageRequest {

    @NotBlank
    @Size(max = 500)
    private String url;

    private boolean cover;

    @PositiveOrZero
    private int sortOrder;

    public PostImage toEntity() {
        return PostImage.builder()
                .url(url)
                .cover(cover)
                .sortOrder(sortOrder)
                .build();
    }
}
