package com.dorandoran.global.annotation.password;

import com.dorandoran.domain.member.dto.request.JoinRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordMatchValidator
        implements ConstraintValidator<PasswordMatch, JoinRequest> {

    @Override
    public boolean isValid(JoinRequest value, ConstraintValidatorContext context) {
        if (value.getPassword() == null || value.getPassword2() == null) {
            return true;
        }
        return value.getPassword().equals(value.getPassword2());
    }
}
