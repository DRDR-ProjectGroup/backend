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
    @NotNull
    private MemberStatus status;
}
