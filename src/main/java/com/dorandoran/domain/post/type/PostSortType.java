package com.dorandoran.domain.post.type;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum PostSortType {
    LATEST,
    POPULAR;

    @JsonCreator
    public static PostSortType from(String value) {
        return PostSortType.valueOf(value.toUpperCase());
    }
}
