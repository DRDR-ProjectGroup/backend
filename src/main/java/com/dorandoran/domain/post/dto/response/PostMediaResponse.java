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
    private long mediaId;
    private String url;
    private int order;

    public static PostMediaResponse of(PostMedia postMedia, String url) {
        return PostMediaResponse.builder()
                .mediaId(postMedia.getId())
                .url(url)
                .order(postMedia.getSortOrder())
                .build();
    }
}
