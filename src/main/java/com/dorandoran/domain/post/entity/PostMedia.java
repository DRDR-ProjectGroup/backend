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
    private String objectKey;

    @Column(nullable = false)
    private long size;

    @Column(nullable = false)
    private int sortOrder;

    @Builder
    private PostMedia(Post post, MediaType mediaType, String originalName, String storedName, String objectKey, long size, int sortOrder) {
        this.post = post;
        this.mediaType = mediaType;
        this.originalName = originalName;
        this.storedName = storedName;
        this.objectKey = objectKey;
        this.size = size;
        this.sortOrder = sortOrder;
    }

    public static PostMedia createPostMedia(Post post, MediaType mediaType, String originalName, String storedName, String objectKey, long size, int sortOrder) {
        return PostMedia.builder()
                .post(post)
                .mediaType(mediaType)
                .originalName(originalName)
                .storedName(storedName)
                .objectKey(objectKey)
                .size(size)
                .sortOrder(sortOrder)
                .build();
    }

    public void updateOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }
}
