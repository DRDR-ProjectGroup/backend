package com.dorandoran.domain.post.storage;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StoredMedia {
    private String originalName;
    private String storedName;
    private String objectKey;
    private long size;
}
