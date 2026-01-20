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
public class MemberDetailResponse {
    private long memberId;
    private String username;
    private String nickname;
    private String email;
    private String role;
    private String status;
    private String createdAt;
    private String modifiedAt;
    private String deletedAt;

    public static MemberDetailResponse of(Member member) {
        return MemberDetailResponse.builder()
                .memberId(member.getId())
                .username(member.getUsername())
                .nickname(member.getNickname())
                .email(member.getEmail())
                .role(member.getRole().name())
                .status(member.getStatus().name())
                .createdAt(member.getCreatedAt().toString())
                .modifiedAt(member.getModifiedAt().toString())
                .deletedAt(member.getDeletedAt() != null ? member.getDeletedAt().toString() : null)
                .build();
    }
}
