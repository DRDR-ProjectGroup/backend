package com.dorandoran.domain.member.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class EmailRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @DisplayName("이메일 요청 DTO 검증 성공")
    @Test
    void emailRequestValidationSuccess() {
        // given
        EmailRequest emailRequest = new EmailRequest(
                "test@naver.com"
        );

        // when
        Set<ConstraintViolation<EmailRequest>> validate = validator.validate(emailRequest);

        // then
        assertThat(validate).hasSize(0);
    }

    @DisplayName("이메일 요청 DTO 검증 실패")
    @Test
    void emailRequestValidationFailure() {
        // given
        EmailRequest emailRequest = new EmailRequest(
                "test@gmail.com"
        );

        // when
        Set<ConstraintViolation<EmailRequest>> validate = validator.validate(emailRequest);

        // then
        assertThat(validate).hasSize(1);
        assertThat(validate.stream()
                .anyMatch(v -> v.getMessage().equals("naver.com 이메일만 사용할 수 있습니다."))).isTrue();
    }
}