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
public class AuthMemberResponse {
    private Long memberId;
    private String username;
    private String email;
    private String nickname;
    private String role;
    private String status;
    private String accessToken;
    private String refreshToken;

    public static AuthMemberResponse of(Member member, String accessToken, String refreshToken) {
        return AuthMemberResponse.builder()
                .memberId(member.getId())
                .username(member.getUsername())
                .email(member.getEmail())
                .nickname(member.getNickname())
                .role(member.getRole().name())
                .status(member.getStatus().name())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
