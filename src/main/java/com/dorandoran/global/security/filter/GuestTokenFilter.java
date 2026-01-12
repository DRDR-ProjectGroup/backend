package com.dorandoran.global.security.filter;

import com.dorandoran.global.jwt.JWTUtil;
import com.dorandoran.standard.util.ControllerUt;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static com.dorandoran.global.jwt.JWTConstant.GUEST_TOKEN_EXPIRE_SECONDS;
import static com.dorandoran.global.jwt.JWTConstant.GUEST_TOKEN_HEADER;

@Component
@RequiredArgsConstructor
@Slf4j
public class GuestTokenFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 1. 로그인 하지 않은 사용자인지 확인
        String accessToken = FilterUtil.extractAccessToken(request);
        log.debug("GuestTokenFilter - AccessToken: {}", accessToken);

        if (accessToken != null && jwtUtil.isValidAccessToken(accessToken)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. GuestToken 조회 및 검증
        String guestToken = Arrays.stream(Optional.ofNullable(request.getCookies()).orElse(new Cookie[0]))
                .filter(cookie -> GUEST_TOKEN_HEADER.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);

        if (guestToken != null) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. GuestToken 생성 및 응답 헤더에 추가
        ControllerUt.addCookie(
                GUEST_TOKEN_HEADER,
                UUID.randomUUID().toString(),
                GUEST_TOKEN_EXPIRE_SECONDS,
                response
        );

        filterChain.doFilter(request, response);
    }
}
