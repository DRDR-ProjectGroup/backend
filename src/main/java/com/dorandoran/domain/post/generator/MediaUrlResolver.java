package com.dorandoran.domain.post.generator;

import com.dorandoran.domain.post.entity.PostMedia;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;

@Component
@RequiredArgsConstructor
public class MediaUrlResolver {

    private final ObjectProvider<S3Client> s3ClientProvider;

    @Value("${app.s3.bucket}")
    private String bucket;

    public String resolve(PostMedia postMedia) {
        String objectKey = postMedia.getObjectKey();

        if (objectKey == null || objectKey.isBlank()) {
            return objectKey;
        }

        if (objectKey.startsWith("/media")) {
            return "http://localhost:8080" + objectKey;
        }

        if (objectKey.startsWith("http://") || objectKey.startsWith("https://") || objectKey.startsWith("/")) {
            return objectKey;
        }

        S3Client s3Client = s3ClientProvider.getIfAvailable();
        if (s3Client == null || bucket == null || bucket.isBlank()) {
            return objectKey;
        }

        return s3Client.utilities()
                .getUrl(GetUrlRequest.builder()
                        .bucket(bucket)
                        .key(objectKey)
                        .build())
                .toExternalForm();
    }
}
