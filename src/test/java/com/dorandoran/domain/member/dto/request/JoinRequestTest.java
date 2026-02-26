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

class JoinRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @DisplayName("회원가입 Dto 유효성 검증")
    @Test
    void join1() {
        // given
        String username = "test";
        String password = "test@1234";
        String password2 = "test@1234";
        String nickname = "tester";
        String email = "test@naver.com";

        JoinRequest joinRequest =
                new JoinRequest(username, password, password2, nickname, email);

        // when
        Set<ConstraintViolation<JoinRequest>> validate = validator.validate(joinRequest);

        // then
        assertThat(validate).hasSize(0);
    }

    @DisplayName("잘못된 회원가입 정보는 오류가 발생됩니다.")
    @Test
    void join2() {
        // given
        String username = "ㅇ";
        String password = "1234";
        String password2 = "12345";
        String nickname = "test er";
        String email = "test@test.com";

        JoinRequest joinRequest =
                new JoinRequest(username, password, password2, nickname, email);

        // when
        Set<ConstraintViolation<JoinRequest>> validate = validator.validate(joinRequest);

        // then
        assertThat(validate).hasSize(7);
        assertThat(validate.stream()
                .anyMatch(v -> v.getMessage().equals("비밀번호에는 최소 8자이며, 최소 하나의 특수문자가 포함되어야 합니다."))).isTrue();
        assertThat(validate.stream()
                .anyMatch(v -> v.getMessage().equals("naver.com 이메일만 사용할 수 있습니다."))).isTrue();
        assertThat(validate.stream()
                .anyMatch(v -> v.getMessage().equals("아이디는 영문 대소문자와 숫자만 포함할 수 있습니다."))).isTrue();
        assertThat(validate.stream()
                .anyMatch(v -> v.getMessage().equals("비밀번호가 일치하지 않습니다."))).isTrue();
        assertThat(validate.stream()
                .anyMatch(v -> v.getMessage().equals("아이디는 최소 4자 이상이어야 합니다."))).isTrue();
        assertThat(validate.stream()
                .anyMatch(v -> v.getMessage().equals("비밀번호는 최소 8자 이상이어야 합니다."))).isTrue();
        assertThat(validate.stream()
                .anyMatch(v -> v.getMessage().equals("닉네임에는 공백을 포함할 수 없습니다."))).isTrue();
    }
}