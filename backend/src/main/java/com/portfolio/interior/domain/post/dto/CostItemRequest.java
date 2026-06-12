package com.portfolio.interior.domain.post.dto;

import com.portfolio.interior.domain.post.entity.CostCategory;
import com.portfolio.interior.domain.post.entity.CostItem;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CostItemRequest {

    @NotNull
    private CostCategory category;

    @Size(max = 100)
    private String label;

    @NotNull
    @PositiveOrZero
    private Long amount;

    @Size(max = 500)
    private String memo;

    public CostItem toEntity() {
        return CostItem.builder()
                .category(category)
                .label(label)
                .amount(amount)
                .memo(memo)
                .build();
    }
}
