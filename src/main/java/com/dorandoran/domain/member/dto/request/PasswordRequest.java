package com.dorandoran.domain.member.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PasswordRequest {
    @NotBlank
    @Size(min = 8)
    @Pattern(
            regexp = "^(?=.*[!@#$%^&*(),.?\":{}|<>]).+$",
            message = "비밀번호에는 최소 8자이며, 최소 하나의 특수문자가 포함되어야 합니다."
    )
    private String password;

    @NotBlank
    @Size(min = 8)
    @Pattern(
            regexp = "^(?=.*[!@#$%^&*(),.?\":{}|<>]).+$",
            message = "비밀번호에는 최소 8자이며, 최소 하나의 특수문자가 포함되어야 합니다."
    )
    private String newPassword;

    private String newPassword2;
}
