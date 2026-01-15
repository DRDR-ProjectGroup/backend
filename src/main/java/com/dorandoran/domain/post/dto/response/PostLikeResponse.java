package com.dorandoran.domain.post.dto.response;

import com.dorandoran.domain.post.entity.Post;
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

    public static PostLikeResponse of(Post post) {
        return PostLikeResponse.builder()
                .likeCount(post.getLikeCount())
                .build();
    }
}
