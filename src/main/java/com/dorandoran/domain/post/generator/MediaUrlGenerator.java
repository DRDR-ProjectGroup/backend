package com.dorandoran.domain.post.generator;

import com.dorandoran.domain.post.type.MediaType;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class MediaUrlGenerator {

    private static final String BASE_URL = "/media";

    public Path generate(MediaType mediaType, String storedName) {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd"));

        return Path.of(
                BASE_URL,
                date,
                mediaType.name().toLowerCase(),
                storedName
        );
    }
}
