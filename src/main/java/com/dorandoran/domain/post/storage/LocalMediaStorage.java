package com.dorandoran.domain.post.storage;

import com.dorandoran.domain.post.generator.FilePathGenerator;
import com.dorandoran.domain.post.type.MediaType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static java.util.Objects.requireNonNull;

@Component
@RequiredArgsConstructor
public class LocalMediaStorage implements MediaStorage {

    private final FilePathGenerator filePathGenerator;

    @Override
    public StoredMedia save(MultipartFile file, MediaType mediaType) throws IOException {
        Path dirPath = filePathGenerator.generate(mediaType);
        String storedName = UUID.randomUUID() + "_" + new File(requireNonNull(file.getOriginalFilename())).getName();
        Path savePath = dirPath.resolve(storedName);

        Files.createDirectories(dirPath);
        file.transferTo(savePath.toFile());

        return new StoredMedia(
                file.getOriginalFilename(),
                storedName,
                savePath.toString(),
                file.getSize()
        );
    }
}
