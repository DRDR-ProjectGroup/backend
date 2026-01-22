package com.dorandoran.domain.member.dto.request;

import com.dorandoran.domain.member.type.MemberStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MemberStatusRequest {
    @NotNull(message = "회원 상태는 필수 입력 항목입니다.")
    private MemberStatus status;
}
