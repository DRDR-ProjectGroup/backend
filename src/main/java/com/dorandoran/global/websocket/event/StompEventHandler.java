package com.dorandoran.global.websocket.event;

import com.dorandoran.global.websocket.dto.response.MemberListResponse;
import com.dorandoran.global.websocket.service.StompService;
import com.dorandoran.global.websocket.session.StompSessionRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

@Component
@RequiredArgsConstructor
@Slf4j
public class StompEventHandler {

    private final StompSessionRegistry sessionRegistry;
    private final SimpMessagingTemplate messagingTemplate;
    private final StompService stompService;

    @EventListener
    public void handleConnect(SessionConnectEvent event) {
        log.info("STOMP 연결이 성공적으로 이루어졌습니다. 세션 ID: {}", event.getMessage().getHeaders().get("simpSessionId"));
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

        String sessionId = accessor.getSessionId();
        String memberId = (String) accessor.getSessionAttributes().get("memberId");

        int beforeCount = sessionRegistry.getSessionCount(memberId);

        sessionRegistry.addSession(memberId, sessionId);

        int afterCount = sessionRegistry.getSessionCount(memberId);

        if (beforeCount == 0 && afterCount == 1) {
            stompService.broadcastMember();
            log.debug("새로운 사용자가 연결되었습니다. memberId: {}, sessionId: {}", memberId, sessionId);
        }
    }

    @EventListener
    public void handleSubscribe(SessionSubscribeEvent event) {
        log.info("STOMP 구독이 이루어졌습니다. 세션 ID: {}, 목적지: {}", event.getMessage().getHeaders().get("simpSessionId"), event.getMessage().getHeaders().get("simpDestination"));
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

        String destination = accessor.getDestination();

        if ("/sub/chat/users".equals(destination)) {
            String sessionId = accessor.getSessionId();

            MemberListResponse response = stompService.getCurrentMembers();

            // 특정 세션에게만 전송하도록 헤더에 sessionId를 설정하여 전송
            SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
            headerAccessor.setSessionId(sessionId);
            headerAccessor.setLeaveMutable(true);

            messagingTemplate.convertAndSend("/sub/chat/users", response, headerAccessor.getMessageHeaders());
        }
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        log.info("STOMP 연결이 종료되었습니다. 세션 ID: {}", event.getSessionId());
        String sessionId = event.getSessionId();

        String memberId = sessionRegistry.removeSession(sessionId);

        if (memberId != null && sessionRegistry.getSessionCount(memberId) == 0) {
            stompService.broadcastMember();
            log.debug("사용자가 연결을 종료했습니다. memberId: {}, sessionId: {}", memberId, sessionId);
        }
    }
}
