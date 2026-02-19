package com.dorandoran.domain.post.storage;

import com.dorandoran.domain.post.generator.FilePathGenerator;
import com.dorandoran.domain.post.generator.MediaUrlGenerator;
import com.dorandoran.domain.post.type.MediaType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import static java.util.Objects.requireNonNull;

@Component
@Profile("!prod")
@RequiredArgsConstructor
public class LocalMediaStorage implements MediaStorage {

    private final FilePathGenerator filePathGenerator;
    private final MediaUrlGenerator mediaUrlGenerator;

    @Override
    public StoredMedia save(MultipartFile file, MediaType mediaType) throws IOException {
        Path dirPath = filePathGenerator.generate(mediaType);
        String storedName = UUID.randomUUID() + "_" + new File(requireNonNull(file.getOriginalFilename())).getName();
        Path savePath = dirPath.resolve(storedName);

        Path imgUrlPath = mediaUrlGenerator.generate(mediaType, storedName);

        Files.createDirectories(dirPath);
        file.transferTo(savePath.toFile());

        return new StoredMedia(
                file.getOriginalFilename(),
                storedName,
                imgUrlPath.toString(),
                file.getSize()
        );
    }

    @Override
    public void delete(List<String> objectKeys) throws IOException {
        if (objectKeys == null || objectKeys.isEmpty()) {
            return;
        }

        String userHome = System.getProperty("user.home");
        Path mediaRoot = Paths.get(userHome, "doranTemp").toAbsolutePath().normalize();

        for (String objectKey : objectKeys) {
            if (objectKey == null || objectKey.isBlank() || !objectKey.startsWith("/media/")) {
                continue;
            }

            String relativePath = objectKey.substring("/media/".length());
            Path filePath = mediaRoot.resolve(relativePath).normalize();
            if (!filePath.startsWith(mediaRoot)) {
                continue;
            }

            Files.deleteIfExists(filePath);
        }
    }
}
