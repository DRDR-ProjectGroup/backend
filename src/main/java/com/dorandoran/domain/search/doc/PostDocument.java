package com.dorandoran.domain.search.doc;

import com.dorandoran.domain.post.entity.Post;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.time.format.DateTimeFormatter;

@Getter
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class PostDocument {
    private Long id;
    private String title;
    private String content;
    private String author;
    private String category;
    private String createdAt;

    public static PostDocument createDoc(Post post) {
        return PostDocument.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .author(post.getMember().getNickname())
                .category(post.getCategory().getAddress())
                .createdAt(post.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .build();
    }
}
