package com.dorandoran.domain.member.dto.response;

import com.dorandoran.domain.member.entity.Member;
import com.dorandoran.domain.member.type.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberInfoResponse {
    private long memberId;
    private String username;
    private String nickname;
    private String email;
    private Role role;

    public static MemberInfoResponse of(Member member) {
        return MemberInfoResponse.builder()
                .memberId(member.getId())
                .username(member.getUsername())
                .nickname(member.getNickname())
                .email(member.getEmail())
                .role(member.getRole())
                .build();
    }
}
