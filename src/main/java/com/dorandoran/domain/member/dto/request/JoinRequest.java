package com.dorandoran.domain.member.dto.request;

import com.dorandoran.global.annotation.email.NaverEmail;
import com.dorandoran.global.annotation.password.PasswordMatch;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@PasswordMatch
public class JoinRequest {

    @NotBlank(message = "아이디는 필수 입력 항목입니다.")
    @Size(min = 4)
    @Pattern(
            regexp = "^[a-zA-Z0-9]+$",
            message = "아이디는 영문 대소문자와 숫자만 포함할 수 있습니다."
    )
    private String username;

    @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    @Size(min = 8)
    @Pattern(
            regexp = "^(?=.*[!@#$%^&*(),.?\":{}|<>]).+$",
            message = "비밀번호에는 최소 8자이며, 최소 하나의 특수문자가 포함되어야 합니다."
    )
    private String password;

    @NotBlank(message = "비밀번호 확인은 필수 입력 항목입니다.")
    private String password2;

    @NotBlank(message = "닉네임은 필수 입력 항목입니다.")
    @Pattern(
            regexp = "^[^\\s]+$",
            message = "닉네임에는 공백을 포함할 수 없습니다."
    )
    private String nickname;

    @NaverEmail
    private String email;
}
