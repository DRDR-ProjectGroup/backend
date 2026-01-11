package com.dorandoran.domain.post.storage;

import com.dorandoran.domain.post.type.MediaType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Component
@Profile("prod")
@RequiredArgsConstructor
public class S3MediaStorage implements MediaStorage {

    @Override
    public StoredMedia save(MultipartFile file, MediaType mediaType) throws IOException {
        // S3에 파일을 저장하는 로직을 여기에 구현합니다.
        // 현재는 구현되지 않았으므로 UnsupportedOperationException을 던집니다.
        throw new UnsupportedOperationException("S3MediaStorage is not implemented yet.");
//        return new StoredMedia(
//                file.getOriginalFilename(),
//                storedName,
//                s3Url,
//                file.getSize()
//        );
    }
}
