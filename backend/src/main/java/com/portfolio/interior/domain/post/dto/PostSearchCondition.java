package com.portfolio.interior.domain.post.dto;

import com.portfolio.interior.domain.post.entity.PostCategory;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostSearchCondition {

    /** SELF / TURNKEY */
    private PostCategory category;

    private String regionCity;

    private String regionDistrict;

    private Double areaPyeongMin;

    private Double areaPyeongMax;

    private Long totalCostMin;

    private Long totalCostMax;

    /** OR 조건: 하나라도 일치하면 포함 */
    private List<String> styleTags;

    /** 제목 + 본문 LIKE 검색 */
    private String keyword;

    private PostSortType sort;

    public PostSortType getSortOrDefault() {
        return sort == null ? PostSortType.LATEST : sort;
    }
}
