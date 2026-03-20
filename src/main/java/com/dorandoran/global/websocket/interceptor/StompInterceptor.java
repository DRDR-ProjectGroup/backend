package com.dorandoran.global.websocket.interceptor;

import com.dorandoran.global.exception.CustomException;
import com.dorandoran.global.jwt.JWTUtil;
import com.dorandoran.global.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
@RequiredArgsConstructor
@Slf4j
public class StompInterceptor implements ChannelInterceptor {

    private final JWTUtil jwtUtil;

    // CONNECT - 연결 될 때 JWT 검증
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = accessor.getFirstNativeHeader("Authorization");

            if (token == null || token.isBlank()) {
                throw new CustomException(ErrorCode.INVALID_TOKEN);
            }

            // Bearer 접두어가 붙어있을 수 있으므로 제거
            token = token.replaceFirst("Bearer\\s+", "").trim();

            // 토큰 유효성 검사
            if (!jwtUtil.isValidAccessToken(token)) {
                throw new CustomException(ErrorCode.INVALID_TOKEN);
            }

            // 토큰에서 사용자 정보 추출
            String memberId = jwtUtil.getUserId(token);
            String role = jwtUtil.getRole(token);

            // Authentication 객체를 Principal로 설정
            Authentication auth = new UsernamePasswordAuthenticationToken(
                    memberId,
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority(role))
            );

            accessor.setUser(auth);

            accessor.getSessionAttributes().put("token", token);
            accessor.getSessionAttributes().put("memberId", memberId);

        } else if (StompCommand.SEND.equals(accessor.getCommand()) || StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            String token = (String) accessor.getSessionAttributes().get("token");

            if (token == null || !jwtUtil.isValidAccessToken(token)) {
                throw new CustomException(ErrorCode.INVALID_TOKEN);
            }
        }

        return message;
    }
}
