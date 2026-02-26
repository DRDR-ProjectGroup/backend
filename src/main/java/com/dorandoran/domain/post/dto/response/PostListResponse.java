package com.dorandoran.domain.post.dto.response;

import com.dorandoran.domain.category.dto.response.CategoryResponse;
import com.dorandoran.domain.member.dto.response.AuthorResponse;
import com.dorandoran.domain.post.entity.Post;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostListResponse {
    private Long postId;
    private String title;
    private String content;
    private AuthorResponse author;
    private CategoryResponse category;
    private int viewCount;
    private int likeCount;
    private int commentCount;
    private boolean isNotice;
    private LocalDateTime createdAt;

    public static PostListResponse of(Post post) {
        return PostListResponse.builder()
                .postId(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .author(AuthorResponse.of(post.getMember()))
                .category(CategoryResponse.of(post.getCategory()))
                .viewCount(post.getViewCount())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .isNotice(post.isNotice())
                .createdAt(post.getCreatedAt())
                .build();
    }
}
