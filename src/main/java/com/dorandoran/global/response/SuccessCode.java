package com.dorandoran.global.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SuccessCode {

    LOGIN_SUCCESS(HttpStatus.OK, "로그인 성공"),
    LOGOUT_SUCCESS(HttpStatus.OK, "로그아웃 성공"),
    JOIN_SUCCESS(HttpStatus.CREATED, "회원가입 성공"),
    RESIGN_SUCCESS(HttpStatus.OK, "회원탈퇴 성공"),

    TOKEN_REISSUE_SUCCESS(HttpStatus.OK, "토큰 재발급 성공"),

    // Common
    SUCCESS(HttpStatus.OK, "요청 응답 성공"),
    ;

    private final HttpStatus httpStatus;
    private final String message;
}