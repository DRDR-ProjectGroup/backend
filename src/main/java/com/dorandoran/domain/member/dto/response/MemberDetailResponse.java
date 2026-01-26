package com.dorandoran.domain.member.dto.response;

import com.dorandoran.domain.member.entity.Member;
import com.dorandoran.domain.member.type.MemberStatus;
import com.dorandoran.domain.member.type.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberDetailResponse {
    private long memberId;
    private String username;
    private String nickname;
    private String email;
    private Role role;
    private MemberStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private LocalDateTime deletedAt;

    public static MemberDetailResponse of(Member member) {
        return MemberDetailResponse.builder()
                .memberId(member.getId())
                .username(member.getUsername())
                .nickname(member.getNickname())
                .email(member.getEmail())
                .role(member.getRole())
                .status(member.getStatus())
                .createdAt(member.getCreatedAt())
                .modifiedAt(member.getModifiedAt())
                .deletedAt(member.getDeletedAt() != null ? member.getDeletedAt() : null)
                .build();
    }
}
