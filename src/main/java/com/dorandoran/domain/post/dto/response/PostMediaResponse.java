package com.dorandoran.domain.post.dto.response;

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

    public static PostMediaResponse of(String url, int order) {
        return PostMediaResponse.builder()
                .url(url)
                .order(order)
                .build();
    }
}
