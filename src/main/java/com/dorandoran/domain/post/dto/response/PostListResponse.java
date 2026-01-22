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
public class PostListResponse {
    private String postId;
    private String title;
    private String content;
    private String author;
    private String category;
    private int viewCount;
    private int likeCount;
    private int commentCount;
    private boolean isNotice;
    private String createdAt;

    public static PostListResponse of(Post post) {
        return PostListResponse.builder()
                .postId(post.getId().toString())
                .title(post.getTitle())
                .content(post.getContent())
                .author(post.getMember().getNickname())
                .category(post.getCategory().getName())
                .viewCount(post.getViewCount())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .isNotice(post.isNotice())
                .createdAt(post.getCreatedAt().toString())
                .build();
    }
}
