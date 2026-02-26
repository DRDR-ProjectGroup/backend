package com.dorandoran.domain.post.entity;

import com.dorandoran.domain.post.type.MediaType;
import com.dorandoran.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostRevisionMedia extends BaseEntity {

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private PostRevision postRevision;

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
    private PostRevisionMedia(
            PostRevision postRevision,
            MediaType mediaType,
            String originalName,
            String storedName,
            String objectKey,
            long size,
            int sortOrder
    ) {
        this.postRevision = postRevision;
        this.mediaType = mediaType;
        this.originalName = originalName;
        this.storedName = storedName;
        this.objectKey = objectKey;
        this.size = size;
        this.sortOrder = sortOrder;
    }

    public static PostRevisionMedia createPostRevisionMedia(PostRevision postRevision, PostMedia postMedia) {
        return PostRevisionMedia.builder()
                .postRevision(postRevision)
                .mediaType(postMedia.getMediaType())
                .originalName(postMedia.getOriginalName())
                .storedName(postMedia.getStoredName())
                .objectKey(postMedia.getObjectKey())
                .size(postMedia.getSize())
                .sortOrder(postMedia.getSortOrder())
                .build();
    }
}
