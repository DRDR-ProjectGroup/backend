package com.dorandoran.global.exception;

import com.dorandoran.global.response.BaseResponse;
import com.dorandoran.global.response.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Arrays;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 사용자 설정 Exception 발생
     */
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<BaseResponse<Void>> customException(CustomException e) {
        BaseResponse<Void> response = BaseResponse.fail(e.getCode());
        return ResponseEntity.status(response.httpStatus()).body(response);
    }

    /**
     * Request Dto 필드 유효성 검증 실패
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<BaseResponse<Void>> validationException(BindException e) {
        List<ObjectError> allErrors = e.getBindingResult().getAllErrors();
        StringBuilder message = new StringBuilder();
        for (ObjectError allError : allErrors) {
            if (!message.isEmpty()) {
                message.append("\n");
            }
            message.append(allError.getDefaultMessage());
        }

        BaseResponse<Void> response = new BaseResponse<>(
                ErrorCode.VALIDATION_FAIL_ERROR.getHttpStatus(),
                message.toString(),
                ErrorCode.VALIDATION_FAIL_ERROR.getHttpStatus().value(),
                null);

        return new ResponseEntity<>(response, response.httpStatus());
    }

    /**
     * Http Method 가 지원되지 않는 Method
     * EX) user/signup (회원가입) 은, `post` 이나, `get` 으로 진행할 경우.
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<BaseResponse<Void>> httpRequestMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        BaseResponse<Void> response = new BaseResponse<>(
                ErrorCode.NOT_SUPPORTED_METHOD.getHttpStatus(),
                e.getMessage(),
                ErrorCode.NOT_SUPPORTED_METHOD.getHttpStatus().value(),
                null);
        return new ResponseEntity<>(response, ErrorCode.NOT_SUPPORTED_METHOD.getHttpStatus());
    }

    /**
     * 404
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<BaseResponse<Void>> noResourceFoundException(NoResourceFoundException e) {
        BaseResponse<Void> response = new BaseResponse<>(
                ErrorCode.NOT_FOUND_URL.getHttpStatus(),
                ErrorCode.NOT_FOUND_URL.getMessage(),
                ErrorCode.NOT_FOUND_URL.getHttpStatus().value(),
                null);
        return new ResponseEntity<>(response, ErrorCode.NOT_FOUND_URL.getHttpStatus());
    }

    /**
     * 핸들링 되지 않은 Exception 발생
     * 로깅 처리 및 오류 발생 응답
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<Void>> notHandledException(Exception e) {
        BaseResponse<Void> response = new BaseResponse<>(
                ErrorCode.FAIL.getHttpStatus(),
                ErrorCode.FAIL.getMessage(),
                ErrorCode.FAIL.getHttpStatus().value(),
                null);

        log.error(Arrays.toString(e.getStackTrace()));
        return new ResponseEntity<>(response, ErrorCode.FAIL.getHttpStatus());
    }

    /**
     * 저장 / 업데이트 등, 실행 시 데이터 무결성 검증 실패로 인한 오류 발생
     * 1. NOT NULL 제약 조건 위반
     * 2. 유니크(Unique) 제약 조건 위반
     * 3. 외래 키(Foreign Key) 제약 조건 위반
     * 4. 참조 무결성 위반 (Foreign Key Deletion or Update)
     * 5. 데이터 타입 위반
     * 6. 문자열 길이 초과
     * 7. 기타 데이터베이스 무결성 제약 조건 위반
     * 등등
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<BaseResponse<Void>> dataIntegrityViolation(DataIntegrityViolationException e) {
        BaseResponse<Void> response = new BaseResponse<>(
                ErrorCode.INVALID_REQUEST_DATA.getHttpStatus(),
                ErrorCode.INVALID_REQUEST_DATA.getMessage(),
                ErrorCode.INVALID_REQUEST_DATA.getHttpStatus().value(),
                null);
        return new ResponseEntity<>(response, ErrorCode.INVALID_REQUEST_DATA.getHttpStatus());
    }
}
