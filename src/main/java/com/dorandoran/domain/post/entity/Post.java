package com.dorandoran.domain.post.entity;

import com.dorandoran.domain.category.entity.Category;
import com.dorandoran.domain.comment.entity.Comment;
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
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseTime {

    @Version
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn()
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn()
    private Category category;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private int viewCount;

    @Column(nullable = false)
    private int likeCount;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @Column(nullable = false)
    private boolean isNotice;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostMedia> postMediaList = new ArrayList<>();

    private LocalDateTime deletedAt;

    private LocalDateTime popularAt;

    @Builder
    private Post(Member member, Category category, String title, String content) {
        this.member = member;
        this.category = category;
        this.title = title;
        this.content = content;
        this.viewCount = 0;
        this.likeCount = 0;
        this.isNotice = false;
    }

    public static Post createPost(Member member, Category category, String title, String content) {
        return Post.builder()
                .member(member)
                .category(category)
                .title(title)
                .content(content)
                .build();
    }

    public void addMedia(PostMedia postMedia) {
        if (postMedia == null) return;
        postMedia.setPost(this);
        this.postMediaList.add(postMedia);
    }

    public void clearMedia() {
        this.postMediaList.clear();
    }

    public void modifyTitleAndContent(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public void setDeletedAt() {
        this.deletedAt = LocalDateTime.now();
    }

    public void setPopularAt(int threshold) {
        if (this.getLikeCount() >= threshold && this.getPopularAt() == null) {
            this.popularAt = LocalDateTime.now();
        }
    }

    public void changeLikeCount(int delta) {
        this.likeCount += delta;
    }

    public void changeNoticeStatus(boolean isNotice) {
        this.isNotice = isNotice;
    }

    public int getCommentCount() {
        return this.comments.size();
    }
}
