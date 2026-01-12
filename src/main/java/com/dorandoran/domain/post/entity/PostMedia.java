package com.dorandoran.domain.post.entity;

import com.dorandoran.domain.post.type.MediaType;
import com.dorandoran.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostMedia extends BaseEntity {

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Post post;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MediaType mediaType;

    @Column(nullable = false)
    private String originalName;

    @Column(nullable = false)
    private String storedName;

    @Column(nullable = false)
    private String url;

    @Column(nullable = false)
    private long size;

    @Column(nullable = false)
    private int sortOrder;

    @Builder
    private PostMedia(Post post, MediaType mediaType, String originalName, String storedName, String url, long size, int sortOrder) {
        this.post = post;
        this.mediaType = mediaType;
        this.originalName = originalName;
        this.storedName = storedName;
        this.url = url;
        this.size = size;
        this.sortOrder = sortOrder;
    }

    public static PostMedia createPostMedia(Post post, MediaType mediaType, String originalName, String storedName, String url, long size, int sortOrder) {
        return PostMedia.builder()
                .post(post)
                .mediaType(mediaType)
                .originalName(originalName)
                .storedName(storedName)
                .url(url)
                .size(size)
                .sortOrder(sortOrder)
                .build();
    }
}
