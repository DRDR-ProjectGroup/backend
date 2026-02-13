package com.dorandoran.domain.comment.dto.response;

import com.dorandoran.domain.post.entity.Post;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentCountResponse {
    private Long postId;
    private int commentCount;

    public static CommentCountResponse of(Post post) {
        return CommentCountResponse.builder()
                .postId(post.getId())
                .commentCount(post.getCommentCount())
                .build();
    }
}
