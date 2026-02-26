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
public class AuthMemberResponse {
    private Long memberId;
    private String username;
    private String email;
    private String nickname;
    private Role role;
    private MemberStatus status;
    private String accessToken;
    private String refreshToken;
    private String guestToken;

    public static AuthMemberResponse of(Member member, String accessToken, String refreshToken, String guestToken) {
        return AuthMemberResponse.builder()
                .memberId(member.getId())
                .username(member.getUsername())
                .email(member.getEmail())
                .nickname(member.getNickname())
                .role(member.getRole())
                .status(member.getStatus())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .guestToken(guestToken)
                .build();
    }
}
