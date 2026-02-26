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

class LoginRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @DisplayName("로그인 요청 DTO 유효성 검증")
    @Test
    void validateLoginRequest() {
        // given
        LoginRequest loginRequest = new LoginRequest("username", "password");

        // when
        Set<ConstraintViolation<LoginRequest>> validate = validator.validate(loginRequest);

        // then
        assertThat(validate).hasSize(0);
    }

    @DisplayName("빈 필드가 있는 로그인 요청 DTO 유효성 검증 실패")
    @Test
    void validateLoginRequest_Failure() {
        // given
        LoginRequest loginRequest = new LoginRequest("", "");

        // when
        Set<ConstraintViolation<LoginRequest>> validate = validator.validate(loginRequest);

        // then
        assertThat(validate).hasSize(2);
    }
}