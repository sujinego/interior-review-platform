package com.portfolio.interior.domain.post.dto;

import com.portfolio.interior.domain.post.entity.CostCategory;
import com.portfolio.interior.domain.post.entity.CostItem;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CostItemResponse {

    private Long id;
    private CostCategory category;
    private String label;
    private Long amount;
    private String memo;

    public static CostItemResponse from(CostItem costItem) {
        return CostItemResponse.builder()
                .id(costItem.getId())
                .category(costItem.getCategory())
                .label(costItem.getLabel())
                .amount(costItem.getAmount())
                .memo(costItem.getMemo())
                .build();
    }
}
