package com.dorandoran.global.exception;

import com.dorandoran.global.response.ErrorCode;
import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {

    private final ErrorCode code;

    public CustomException(ErrorCode code) {
        super(code.getHttpStatus() + " : " + code.getMessage());
        this.code = code;
    }

    public CustomException(ErrorCode code, Throwable cause) {
        super(code.getHttpStatus() + " : " + code.getMessage(), cause);
        this.code = code;
    }
}
