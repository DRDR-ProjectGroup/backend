package com.dorandoran.domain.post.storage;

import com.dorandoran.domain.post.type.MediaType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import static java.util.Objects.requireNonNull;

@Component
@Profile("prod")
@RequiredArgsConstructor
public class S3MediaStorage implements MediaStorage {

    private final S3Client s3Client;

    @Value("${app.s3.bucket}")
    private String bucket;

    @Value("${app.s3.prefix}")
    private String prefix;

    @Override
    public StoredMedia save(MultipartFile file, MediaType mediaType) throws IOException {
        String originalName = file.getOriginalFilename();
        String safeOriginalName = new File(requireNonNull(originalName)).getName();
        String storedName = UUID.randomUUID() + "_" + safeOriginalName;
        String objectKey = generateObjectKey(mediaType, storedName);

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .contentType(file.getContentType())
                .contentLength(file.getSize())
                .build();

        s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        return new StoredMedia(
                originalName,
                storedName,
                objectKey,
                file.getSize()
        );
    }

    @Override
    public void delete(List<String> objectKeys) {
        if (objectKeys == null || objectKeys.isEmpty()) {
            return;
        }

        List<ObjectIdentifier> identifiers = objectKeys.stream()
                .filter(key -> key != null && !key.isBlank())
                .map(key -> ObjectIdentifier.builder().key(key).build())
                .toList();

        if (identifiers.isEmpty()) {
            return;
        }

        for (int i = 0; i < identifiers.size(); i += 1000) {
            List<ObjectIdentifier> batch = identifiers.subList(i, Math.min(i + 1000, identifiers.size()));
            DeleteObjectsRequest request = DeleteObjectsRequest.builder()
                    .bucket(bucket)
                    .delete(Delete.builder().objects(batch).build())
                    .build();
            s3Client.deleteObjects(request);
        }
    }

    private String generateObjectKey(MediaType mediaType, String storedName) {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        String normalizedPrefix = prefix == null ? "" : prefix.trim();
        if (!normalizedPrefix.isEmpty() && !normalizedPrefix.endsWith("/")) {
            normalizedPrefix += "/";
        }

        return normalizedPrefix + date + "/" + mediaType.name().toLowerCase() + "/" + storedName;
    }
}
