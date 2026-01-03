package com.dorandoran.global.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.http.HttpStatus;

public record BaseResponse<T>(
        @JsonIgnore HttpStatus httpStatus,
        String message,
        int code,
        T data
) {
    @JsonIgnore
    public boolean isSuccess() {
        return !httpStatus.isError();
    }

    public static <T> BaseResponse<T> ok(SuccessCode code, T data) {
        return new BaseResponse<>(
                code.getHttpStatus(),
                code.getMessage(),
                code.getHttpStatus().value(),
                data
        );
    }

    public static BaseResponse<Void> ok() {
        return new BaseResponse<>(
                SuccessCode.SUCCESS.getHttpStatus(),
                SuccessCode.SUCCESS.getMessage(),
                SuccessCode.SUCCESS.getHttpStatus().value(),
                null
        );
    }

    public static BaseResponse<Void> ok(SuccessCode code) {
        return new BaseResponse<>(
                code.getHttpStatus(),
                code.getMessage(),
                code.getHttpStatus().value(),
                null
        );
    }

    // 실패 응답 생성 팩토리 메서드
    public static BaseResponse<Void> fail(ErrorCode code) {
        return new BaseResponse<>(
                code.getHttpStatus(),
                code.getMessage(),
                code.getHttpStatus().value(),
                null
        );
    }

    // 실패 응답 생성 팩토리 메서드
    public static BaseResponse<Void> fail(ErrorCode code, String message) {
        return new BaseResponse<>(
                code.getHttpStatus(),
                message,
                code.getHttpStatus().value(),
                null
        );
    }
}
