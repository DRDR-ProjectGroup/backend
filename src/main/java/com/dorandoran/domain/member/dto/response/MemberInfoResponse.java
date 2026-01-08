package com.dorandoran.domain.member.dto.response;

import com.dorandoran.domain.member.entity.Member;
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

    public static MemberInfoResponse of(Member member) {
        return MemberInfoResponse.builder()
                .memberId(member.getId())
                .username(member.getUsername())
                .nickname(member.getNickname())
                .email(member.getEmail())
                .build();
    }
}
