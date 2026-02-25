package com.dorandoran.domain.post.entity;

import com.dorandoran.domain.category.entity.Category;
import com.dorandoran.domain.member.entity.Member;
import com.dorandoran.global.jpa.entity.BaseTime;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(indexes = {
        @Index(name = "idx_post_revision_original_post_id", columnList = "originalPostId")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostRevision extends BaseTime {

    @Version
    private Long version;

    @Column(nullable = false)
    private Long originalPostId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Category category;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private int viewCount;

    @Column(nullable = false)
    private int likeCount;

    @Column(nullable = false)
    private boolean isNotice;

    @OneToMany(mappedBy = "postRevision", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostRevisionMedia> postRevisionMediaList = new ArrayList<>();

    private LocalDateTime deletedAt;

    private LocalDateTime popularAt;

    private LocalDateTime postCreatedAt;

    private LocalDateTime postModifiedAt;

    @Builder
    private PostRevision(
            Long originalPostId,
            Member member,
            Category category,
            String title,
            String content,
            int viewCount,
            int likeCount,
            boolean isNotice,
            LocalDateTime deletedAt,
            LocalDateTime popularAt,
            LocalDateTime postCreatedAt,
            LocalDateTime postModifiedAt
    ) {
        this.originalPostId = originalPostId;
        this.member = member;
        this.category = category;
        this.title = title;
        this.content = content;
        this.viewCount = viewCount;
        this.likeCount = likeCount;
        this.isNotice = isNotice;
        this.deletedAt = deletedAt;
        this.popularAt = popularAt;
        this.postCreatedAt = postCreatedAt;
        this.postModifiedAt = postModifiedAt;
    }

    public static PostRevision createPostRevision(Post post) {
        PostRevision revision = PostRevision.builder()
                .originalPostId(post.getId())
                .member(post.getMember())
                .category(post.getCategory())
                .title(post.getTitle())
                .content(post.getContent())
                .viewCount(post.getViewCount())
                .likeCount(post.getLikeCount())
                .isNotice(post.isNotice())
                .deletedAt(post.getDeletedAt())
                .popularAt(post.getPopularAt())
                .postCreatedAt(post.getCreatedAt())
                .postModifiedAt(post.getModifiedAt())
                .build();

        for (PostMedia postMedia : post.getPostMediaList()) {
            revision.addPostRevisionMedia(PostRevisionMedia.createPostRevisionMedia(revision, postMedia));
        }

        return revision;
    }

    public void addPostRevisionMedia(PostRevisionMedia postRevisionMedia) {
        if (postRevisionMedia == null) {
            return;
        }
        postRevisionMedia.setPostRevision(this);
        this.postRevisionMediaList.add(postRevisionMedia);
    }
}
