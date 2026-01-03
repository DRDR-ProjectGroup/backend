package com.dorandoran.global.security;

import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.List;
import java.util.Map;

@Getter
public class SecurityUser extends User {
    private final long id;
    private final String email;
    private final boolean isNewUser;
    private final Map<String, Object> attributes;

    // 일반 로그인용 생성자
    public SecurityUser(long id, String username, String email, String role) {
        super(username, "", List.of(new SimpleGrantedAuthority(role)));
        this.id = id;
        this.email = email;
        this.isNewUser = false;
        this.attributes = null;
    }

    public static SecurityUser of(long id, String username, String email, String role) {
        return new SecurityUser(id, username, email, role);
    }

}