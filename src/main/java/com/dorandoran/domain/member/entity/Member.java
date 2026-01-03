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

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    public void withdraw() {
        this.status = MemberStatus.DELETED;
        this.deletedAt = LocalDateTime.now();
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
