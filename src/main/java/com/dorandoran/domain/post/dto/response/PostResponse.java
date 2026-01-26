package com.dorandoran.domain.post.dto.response;

import com.dorandoran.domain.member.dto.response.AuthorResponse;
import com.dorandoran.domain.post.entity.Post;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostResponse {
    private String postId;
    private String title;
    private String content;
    private AuthorResponse author;
    private int viewCount;
    private int likeCount;
    private String category;
    private List<PostMediaResponse> mediaList;
    private boolean isNotice;
    private String createdAt;

    public static PostResponse of(Post post, List<PostMediaResponse> mediaList) {
        return PostResponse.builder()
                .postId(post.getId().toString())
                .title(post.getTitle())
                .content(post.getContent())
                .author(AuthorResponse.of(post.getMember()))
                .viewCount(post.getViewCount())
                .likeCount(post.getLikeCount())
                .category(post.getCategory().getName())
                .mediaList(mediaList)
                .isNotice(post.isNotice())
                .createdAt(post.getCreatedAt().toString())
                .build();

    }
}
