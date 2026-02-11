package com.dorandoran.domain.post.dto.response;

import com.dorandoran.domain.post.entity.Post;
import com.dorandoran.domain.post.entity.PostLike;
import com.dorandoran.domain.post.type.LikeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostLikeResponse {
    private int likeCount;
    private LikeType memberLikeType;

    public static PostLikeResponse of(Post post, PostLike postLike) {
        return PostLikeResponse.builder()
                .likeCount(post.getLikeCount())
                .memberLikeType(postLike != null ? postLike.getLikeType() : null)
                .build();
    }
}
