package com.dorandoran.domain.member.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberTokenResponse {

    private String accessToken;
    private String refreshToken;

    public static MemberTokenResponse of(String accessToken, String refreshToken) {
        return MemberTokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
