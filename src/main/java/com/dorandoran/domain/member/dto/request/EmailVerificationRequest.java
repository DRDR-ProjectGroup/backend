package com.dorandoran.domain.member.dto.request;

import com.dorandoran.global.annotation.email.NaverEmail;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EmailVerificationRequest {
    @NaverEmail
    private String email;

    @Min(100000)
    @Max(999999)
    private int code;
}
