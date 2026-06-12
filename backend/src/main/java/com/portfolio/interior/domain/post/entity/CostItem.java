package com.portfolio.interior.domain.post.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "cost_items")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CostItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CostCategory category;

    @Column(length = 100)
    private String label;

    @Column(nullable = false)
    private Long amount;

    @Column(length = 500)
    private String memo;

    @Builder
    public CostItem(CostCategory category, String label, Long amount, String memo) {
        this.category = category;
        this.label = label;
        this.amount = amount;
        this.memo = memo;
    }

    void setPost(Post post) {
        this.post = post;
    }
}
