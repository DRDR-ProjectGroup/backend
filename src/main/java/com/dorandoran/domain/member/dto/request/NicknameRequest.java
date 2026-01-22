package com.dorandoran.domain.member.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NicknameRequest {
    @NotBlank(message = "닉네임은 필수 입력 항목입니다.")
    @Pattern(
            regexp = "^[^\\s]+$",
            message = "닉네임에는 공백을 포함할 수 없습니다."
    )
    private String newNickname;
}
