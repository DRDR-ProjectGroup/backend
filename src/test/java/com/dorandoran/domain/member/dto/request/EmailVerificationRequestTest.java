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

class EmailVerificationRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @DisplayName("이메일 인증 요청 DTO 유효성 검증 성공")
    @Test
    void validateEmailVerificationRequest_Success() {
        // given
        EmailVerificationRequest request = new EmailVerificationRequest(
                "test@naver.com",
                123456
        );

        // when
        Set<ConstraintViolation<EmailVerificationRequest>> validate = validator.validate(request);

        // then
        assertThat(validate).hasSize(0);
    }

    @DisplayName("이메일 인증 요청 DTO 유효성 검증 실패")
    @Test
    void validateEmailVerificationRequest_Failure() {
        // given
        EmailVerificationRequest request = new EmailVerificationRequest(
                "test@test.com",
                1234
        );

        // when
        Set<ConstraintViolation<EmailVerificationRequest>> validate = validator.validate(request);

        // then
        assertThat(validate).hasSize(2);
        assertThat(validate.stream()
                .anyMatch(v -> v.getMessage().equals("naver.com 이메일만 사용할 수 있습니다."))).isTrue();
        assertThat(validate.stream()
                .anyMatch(v -> v.getMessage().equals("100000 이상이어야 합니다"))).isTrue();
    }

}