package com.dorandoran.domain.comment.entity;

import com.dorandoran.domain.member.entity.Member;
import com.dorandoran.domain.post.entity.Post;
import com.dorandoran.global.jpa.entity.BaseTime;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseTime {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private Comment parentComment;

    private int depth;

    @Column(nullable = false)
    private String content;

    private LocalDateTime deletedAt;

    @Builder
    public Comment(Post post, Member member, Comment parentComment, int depth, String content) {
        this.post = post;
        this.member = member;
        this.parentComment = parentComment;
        this.depth = depth;
        this.content = content;
    }

    public static Comment createComment(Post post, Member member, Comment parentComment, String content) {
        int depth = (parentComment == null) ? 0 : parentComment.getDepth() + 1;
        return Comment.builder()
                .post(post)
                .member(member)
                .parentComment(parentComment)
                .depth(depth)
                .content(content)
                .build();
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }
}
