package com.dorandoran.domain.post.dto.request;

import com.dorandoran.SpringBootTestSupporter;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PostCreateRequestTest extends SpringBootTestSupporter {

    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @DisplayName("title 공백 검증")
    @Test
    void validateTitleNotBlank() {
        // given
        PostCreateRequest request = new PostCreateRequest("", "content");

        // when
        Set<ConstraintViolation<PostCreateRequest>> validate = validator.validate(request);

        // then
        assertThat(validate).isNotEmpty();
    }

    @DisplayName("content 공백 검증")
    @Test
    void validateContentNotBlank() {
        // given
        PostCreateRequest request = new PostCreateRequest("title", "");

        // when
        Set<ConstraintViolation<PostCreateRequest>> validate = validator.validate(request);

        // then
        assertThat(validate).isNotEmpty();
    }
}