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
    @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    @Size(min = 8)
    @Pattern(
            regexp = "^(?=.*[!@#$%^&*(),.?\":{}|<>]).+$",
            message = "비밀번호에는 최소 8자이며, 최소 하나의 특수문자가 포함되어야 합니다."
    )
    private String password;

    @NotBlank(message = "새 비밀번호는 필수 입력 항목입니다.")
    @Size(min = 8)
    @Pattern(
            regexp = "^(?=.*[!@#$%^&*(),.?\":{}|<>]).+$",
            message = "비밀번호에는 최소 8자이며, 최소 하나의 특수문자가 포함되어야 합니다."
    )
    private String newPassword;

    @NotBlank(message = "새 비밀번호 확인은 필수 입력 항목입니다.")
    private String newPassword2;
}
