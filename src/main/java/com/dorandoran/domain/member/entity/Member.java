package com.dorandoran.domain.member.entity;

import com.dorandoran.domain.member.type.MemberStatus;
import com.dorandoran.domain.member.type.Role;
import com.dorandoran.global.jpa.entity.BaseTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
// 모든 조회 시 DELETED 상태의 회원 제외
@SQLRestriction("status != 'DELETED'")
// memberRepository.delete() 호출 시 status를 DELETED로 변경, deletedAt 현재 시간으로 설정
@SQLDelete(sql = "UPDATE member SET status = 'DELETED', deleted_at = NOW() WHERE id = ?")
public class Member extends BaseTime {

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberStatus status;

    @Column(nullable = true)
    private LocalDateTime deletedAt;

    @Builder
    private Member(String username, String password, String email, String nickname, Role role, MemberStatus status) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.nickname = nickname;
        this.role = role;
        this.status = status;
    }

    public static Member createMember(String username, String password, String email, String nickname) {
        return Member.builder()
                .username(username)
                .password(password)
                .email(email)
                .nickname(nickname)
                .role(Role.ROLE_MEMBER)
                .status(MemberStatus.ACTIVE)
                .build();
    }


    public static Member createSecurityContextUser(Long userId, Role role) {
        Member member = new Member();
        member.setId(userId);
        member.role = role;
        return member;
    }
}
