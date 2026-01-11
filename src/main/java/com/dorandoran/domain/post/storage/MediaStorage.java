package com.dorandoran.domain.post.storage;

import com.dorandoran.domain.post.type.MediaType;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface MediaStorage {
    StoredMedia save(MultipartFile file, MediaType mediaType) throws IOException;
}
