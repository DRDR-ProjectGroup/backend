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
    MY_POSTS_SUCCESS(HttpStatus.OK, "내 게시글 목록 조회 성공"),
    MY_COMMENTS_SUCCESS(HttpStatus.OK, "내 댓글 목록 조회 성공"),

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

    // Comment
    COMMENT_CREATE_SUCCESS(HttpStatus.CREATED, "댓글 생성 성공"),
    COMMENT_LIST_READ_SUCCESS(HttpStatus.OK, "댓글 목록 조회 성공"),
    COMMENT_MODIFY_SUCCESS(HttpStatus.OK, "댓글 수정 성공"),
    COMMENT_DELETE_SUCCESS(HttpStatus.OK, "댓글 삭제 성공"),

    // Message
    MESSAGE_SEND_SUCCESS(HttpStatus.CREATED, "메세지 전송 성공"),
    MESSAGE_DETAIL_SUCCESS(HttpStatus.OK, "메세지 상세 조회 성공"),
    MESSAGE_LIST_SUCCESS(HttpStatus.OK, "메세지 목록 조회 성공"),
    MESSAGE_DELETE_SUCCESS(HttpStatus.OK, "메세지 삭제 성공"),

    // Category
    CATEGORY_LIST_SUCCESS(HttpStatus.OK, "카테고리 목록 조회 성공"),

    // Admin
    CATEGORY_GROUP_CREATE_SUCCESS(HttpStatus.CREATED, "카테고리 그룹 생성 성공"),
    CATEGORY_GROUP_MODIFY_SUCCESS(HttpStatus.OK, "카테고리 그룹명 수정 성공"),
    CATEGORY_GROUP_DELETE_SUCCESS(HttpStatus.OK, "카테고리 그룹 삭제 성공"),
    CATEGORY_CREATE_SUCCESS(HttpStatus.CREATED, "카테고리 생성 성공"),
    CATEGORY_MODIFY_SUCCESS(HttpStatus.OK, "카테고리명 수정 성공"),
    CATEGORY_DELETE_SUCCESS(HttpStatus.OK, "카테고리 삭제 성공"),
    MEMBER_LIST_DETAIL_SUCCESS(HttpStatus.OK, "회원 상세 목록 조회 성공"),
    MEMBER_STATUS_CHANGE_SUCCESS(HttpStatus.OK, "회원 상태 변경 성공"),

    // Common
    SUCCESS(HttpStatus.OK, "요청 응답 성공"),
    ;

    private final HttpStatus httpStatus;
    private final String message;
}