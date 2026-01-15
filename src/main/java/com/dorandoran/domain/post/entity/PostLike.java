package com.dorandoran.domain.post.entity;

import com.dorandoran.domain.member.entity.Member;
import com.dorandoran.domain.post.type.LikeType;
import com.dorandoran.global.jpa.entity.BaseTime;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_member_post_like",
                        columnNames = {"member_id", "post_id"}
                )
        }
)
public class PostLike extends BaseTime {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @Enumerated(EnumType.STRING)
    private LikeType likeType;

    @Builder
    public PostLike(Member member, Post post, LikeType likeType) {
        this.member = member;
        this.post = post;
        this.likeType = likeType;
    }

    public static PostLike of(Member member, Post post, LikeType likeType) {
        return PostLike.builder()
                .member(member)
                .post(post)
                .likeType(likeType)
                .build();
    }

    public void changeLikeType(LikeType likeType) {
        this.likeType = likeType;
    }
}
