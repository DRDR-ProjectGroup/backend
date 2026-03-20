package com.dorandoran.global.websocket.controller;

import com.dorandoran.domain.member.entity.Member;
import com.dorandoran.domain.member.service.MemberService;
import com.dorandoran.global.exception.CustomException;
import com.dorandoran.global.response.ErrorCode;
import com.dorandoran.global.websocket.dto.request.MessageRequest;
import com.dorandoran.global.websocket.dto.response.MessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;

@Controller
@Slf4j
@RequiredArgsConstructor
public class ChatController {

    private final MemberService memberService;

    @MessageMapping("/chat/message")
    @SendTo("/sub/chat/message")
    public MessageResponse sendMessage(MessageRequest request, Principal principal) {
        if (principal == null) {
            log.warn("메시지 전송 시 인증 정보가 없습니다.");
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        String memberId = principal.getName();

        Member member = memberService.findMemberByStringId(memberId);

        return MessageResponse.of(
                memberId,
                member.getNickname(),
                request.getMessage(),
                LocalDateTime.now()
        );
    }
}
