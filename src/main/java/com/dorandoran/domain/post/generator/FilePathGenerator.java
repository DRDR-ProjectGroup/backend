package com.dorandoran.domain.post.generator;

import com.dorandoran.domain.post.type.MediaType;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class FilePathGenerator {

    private static final String ROOT_DIR = "doranTemp";

    public Path generate(MediaType mediaType) {
        String userHome = System.getProperty("user.home");
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd"));

        return Paths.get(
                userHome,
                ROOT_DIR,
                date,
                mediaType.name().toLowerCase()
        );
    }
}
