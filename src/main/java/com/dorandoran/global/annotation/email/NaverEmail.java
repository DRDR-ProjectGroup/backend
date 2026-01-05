package com.dorandoran.global.annotation.email;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
@NotBlank
@Email
@Pattern(
        regexp = "^[A-Za-z0-9._%+-]+@naver\\.com$",
        message = "naver.com 이메일만 사용할 수 있습니다."
)
public @interface NaverEmail {
    String message() default "유효하지 않은 이메일입니다.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}