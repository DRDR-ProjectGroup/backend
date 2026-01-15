package com.dorandoran.domain.post.type;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum LikeType {
    LIKE,
    DISLIKE;

    @JsonCreator
    public static LikeType from(String value) {
        return LikeType.valueOf(value.toUpperCase());
    }
}
