package com.dorandoran.global.security.provider;

import com.dorandoran.domain.member.type.MemberStatus;
import com.dorandoran.global.exception.CustomException;
import com.dorandoran.global.response.ErrorCode;
import com.dorandoran.global.security.auth.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication)
            throws AuthenticationException {
        String loginId = authentication.getName();
        String password = authentication.getCredentials().toString();

        UserDetails userDetails = userDetailsService.loadUserByUsername(loginId);

        if (userDetails == null) {
            throw new CustomException(ErrorCode.LOGIN_FAIL);
        }

        CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;
        if (customUserDetails.getMember().getStatus().equals(MemberStatus.DELETED)) {
            throw new CustomException(ErrorCode.LOGIN_RESIGN_USER);
        }

        if (!passwordEncoder.matches(password, userDetails.getPassword())) {
            throw new CustomException(ErrorCode.LOGIN_FAIL);
        }

        return new UsernamePasswordAuthenticationToken(userDetails, password, userDetails.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}