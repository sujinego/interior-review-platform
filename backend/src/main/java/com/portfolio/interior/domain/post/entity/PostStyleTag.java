package com.portfolio.interior.domain.post.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "post_style_tags", indexes = {
        @Index(name = "idx_post_style_tags_post_id_tag", columnList = "post_id, tag")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostStyleTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(nullable = false, length = 30)
    private String tag;

    @Builder
    public PostStyleTag(String tag) {
        this.tag = tag;
    }

    void setPost(Post post) {
        this.post = post;
    }
}
