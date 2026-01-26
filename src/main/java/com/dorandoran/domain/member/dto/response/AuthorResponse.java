package com.dorandoran.domain.member.dto.response;

import com.dorandoran.domain.member.entity.Member;
import com.dorandoran.domain.member.type.MemberStatus;
import com.dorandoran.domain.member.type.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthorResponse {
    private long memberId;
    private String nickname;
    private Role role;
    private MemberStatus status;

    public static AuthorResponse of(Member member) {
        return AuthorResponse.builder()
                .memberId(member.getId())
                .nickname(member.getNickname())
                .role(member.getRole())
                .status(member.getStatus())
                .build();
    }
}
