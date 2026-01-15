package com.dorandoran.domain.member.entity;

import com.dorandoran.domain.member.type.MemberStatus;
import com.dorandoran.domain.member.type.Role;
import com.dorandoran.domain.post.entity.Post;
import com.dorandoran.global.jpa.entity.BaseTime;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.List;

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

    @OneToMany(mappedBy = "member")
    private List<Post> posts;

    private LocalDateTime deletedAt;

    @Builder
    private Member(String username, String password, String email, String nickname) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.nickname = nickname;
        this.role = Role.ROLE_MEMBER;
        this.status = MemberStatus.ACTIVE;
    }

    public static Member createMember(String username, String password, String email, String nickname) {
        return Member.builder()
                .username(username)
                .password(password)
                .email(email)
                .nickname(nickname)
                .build();
    }


    public static Member createSecurityContextUser(Long userId, Role role) {
        Member member = new Member();
        member.setId(userId);
        member.role = role;
        return member;
    }

    public void modifyNickname(String nickname) {
        this.nickname = nickname;
    }

    public void modifyPassword(String newPassword) {
        this.password = newPassword;
    }

    public void setRoleAdmin() {
        this.role = Role.ROLE_ADMIN;
    }
}
