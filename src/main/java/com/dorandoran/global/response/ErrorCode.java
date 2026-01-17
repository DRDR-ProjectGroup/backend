package com.dorandoran.global.response;

import com.dorandoran.global.exception.CustomException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Member 오류
    NEED_LOGIN(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 부족합니다."),
    LOGIN_FAIL(HttpStatus.BAD_REQUEST, "잘못된 아이디 혹은 패스워드 입니다."),
    LOGIN_RESIGN_USER(HttpStatus.BAD_REQUEST, "탈퇴된 회원 입니다."),
    DUPLICATED_USERNAME(HttpStatus.BAD_REQUEST, "중복된 아이디입니다."),
    DUPLICATED_NICKNAME(HttpStatus.BAD_REQUEST, "중복된 닉네임입니다."),
    DUPLICATED_EMAIL(HttpStatus.BAD_REQUEST, "중복된 이메일입니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."),
    INVALID_CURRENT_PASSWORD(HttpStatus.BAD_REQUEST, "현재 비밀번호와 일치합니다."),

    // JWT 오류
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),

    // Email 오류
    EMAIL_SEND_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "이메일 전송에 실패했습니다."),
    EMAIL_VERIFY_FAIL(HttpStatus.BAD_REQUEST, "이메일 인증에 실패했습니다."),
    INVALID_AUTH_CODE(HttpStatus.BAD_REQUEST, "유효하지 않은 인증 코드입니다."),
    EXPIRED_AUTH_CODE(HttpStatus.BAD_REQUEST, "만료된 인증 코드입니다."),
    AUTH_CODE_NOT_FOUND(HttpStatus.BAD_REQUEST, "인증 요청 이력이 없습니다. 이메일을 다시 확인해 주세요."),
    EMAIL_NOT_VERIFIED(HttpStatus.BAD_REQUEST, "이메일 인증이 완료되지 않았습니다."),

    // Category 오류
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 카테고리입니다."),

    // Post 오류
    INVALID_MEDIA_TYPE(HttpStatus.BAD_REQUEST, "지원하지 않는 미디어 타입입니다."),
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 게시글입니다."),
    UNAUTHORIZED_POST_MODIFICATION(HttpStatus.UNAUTHORIZED, "게시글 수정/삭제 권한이 없습니다."),

    // Comment 오류
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 댓글입니다."),

    // Message 오류
    CANNOT_SEND_MESSAGE_TO_SELF(HttpStatus.BAD_REQUEST, "자기 자신에게는 메세지를 보낼 수 없습니다."),
    MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 메세지입니다."),
    NO_ACCESS_TO_MESSAGE(HttpStatus.FORBIDDEN, "해당 메세지에 접근할 수 있는 권한이 없습니다."),
    INVALID_MESSAGE_TYPE(HttpStatus.BAD_REQUEST, "유효하지 않은 메세지 타입입니다."),

    // Common 오류,
    VALIDATION_FAIL_ERROR(HttpStatus.BAD_REQUEST, "(exception error 메세지에 따름)"),
    NOT_SUPPORTED_METHOD(HttpStatus.METHOD_NOT_ALLOWED, "(exception error 메세지에 따름"),
    NOT_FOUND_URL(HttpStatus.NOT_FOUND, "요청하신 URL 을 찾을 수 없습니다."),
    INVALID_REQUEST_DATA(HttpStatus.BAD_REQUEST, "데이터 저장 실패, 재시도 혹은 관리자에게 문의해주세요."),
    FAIL(HttpStatus.BAD_REQUEST, "요청 응답 실패, 관리자에게 문의해주세요."),
    FORBIDDEN_ACCESS(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    ;


    private final HttpStatus httpStatus;
    private final String message;

    public CustomException throwCustomException() {
        throw new CustomException(this);
    }

    public CustomException throwCustomException(Throwable cause) {
        throw new CustomException(this, cause);
    }
}