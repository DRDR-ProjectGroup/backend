package com.dorandoran.global.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SuccessCode {

    // Member
    LOGIN_SUCCESS(HttpStatus.OK, "로그인 성공"),
    LOGOUT_SUCCESS(HttpStatus.OK, "로그아웃 성공"),
    JOIN_SUCCESS(HttpStatus.CREATED, "회원가입 성공"),
    RESIGN_SUCCESS(HttpStatus.OK, "회원탈퇴 성공"),
    MEMBER_INFO_SUCCESS(HttpStatus.OK, "회원 정보 조회 성공"),
    NICKNAME_MODIFY_SUCCESS(HttpStatus.OK, "닉네임 수정 성공"),
    PASSWORD_MODIFY_SUCCESS(HttpStatus.OK, "비밀번호 수정 성공"),

    // JWT
    TOKEN_REISSUE_SUCCESS(HttpStatus.OK, "토큰 재발급 성공"),

    // Email
    EMAIL_SEND_SUCCESS(HttpStatus.OK, "이메일 인증 코드 전송 성공"),
    EMAIL_VERIFY_SUCCESS(HttpStatus.OK, "이메일 인증 코드 검증 성공"),

    // Post
    POST_CREATE_SUCCESS(HttpStatus.CREATED, "게시글 생성 성공"),
    POST_MODIFY_SUCCESS(HttpStatus.OK, "게시글 수정 성공"),
    POST_DELETE_SUCCESS(HttpStatus.OK, "게시글 삭제 성공"),
    POST_DETAIL_SUCCESS(HttpStatus.OK, "게시글 상세 조회 성공"),
    POST_LIST_SUCCESS(HttpStatus.OK, "게시글 목록 조회 성공"),
    POST_LIKE_SUCCESS(HttpStatus.OK, "게시글 추천 성공"),
    POST_UNLIKE_SUCCESS(HttpStatus.OK, "게시글 비추천 성공"),
    POST_NOTICE_SUCCESS(HttpStatus.OK, "공지글 설정 변경 성공"),

    // Common
    SUCCESS(HttpStatus.OK, "요청 응답 성공"),
    ;

    private final HttpStatus httpStatus;
    private final String message;
}