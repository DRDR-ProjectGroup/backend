package com.dorandoran.domain.post.dto.response;

import com.dorandoran.domain.post.entity.PostMedia;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostMediaResponse {
    private String url;
    private int order;

    public static PostMediaResponse of(PostMedia postMedia) {
        return PostMediaResponse.builder()
                .url(postMedia.getUrl())
                .order(postMedia.getSortOrder())
                .build();
    }
}
