package com.dorandoran.domain.member.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    @NotBlank(message = "아이디를 확인해주세요.")
    private String username;

    @NotBlank(message = "비밀번호를 확인해주세요.")
    private String password;
}
