package com.dorandoran.domain.post.storage;

import com.dorandoran.domain.post.type.MediaType;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface MediaStorage {
    StoredMedia save(MultipartFile file, MediaType mediaType) throws IOException;

    default void delete(List<String> objectKeys) throws IOException {
    }
}
