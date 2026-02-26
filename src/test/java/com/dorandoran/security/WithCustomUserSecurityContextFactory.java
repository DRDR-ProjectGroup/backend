package com.dorandoran.security;

import com.dorandoran.domain.member.entity.Member;
import com.dorandoran.domain.member.type.Role;
import com.dorandoran.global.security.auth.CustomUserDetails;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class WithCustomUserSecurityContextFactory implements
        WithSecurityContextFactory<WithCustomUser> {

    @Override
    public SecurityContext createSecurityContext(WithCustomUser annotation) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        Member member = Member.createSecurityContextUser(
                Long.parseLong(annotation.username()),
                Role.valueOf(annotation.role()));

        CustomUserDetails customUserDetails = new CustomUserDetails(member);
        Authentication auth = new UsernamePasswordAuthenticationToken(
                customUserDetails, null, customUserDetails.getAuthorities());
        context.setAuthentication(auth);

        return context;

    }
}
