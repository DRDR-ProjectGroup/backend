package com.dorandoran.global.websocket.service;

import com.dorandoran.domain.member.entity.Member;
import com.dorandoran.domain.member.service.MemberService;
import com.dorandoran.global.websocket.dto.response.ChatMemberInfo;
import com.dorandoran.global.websocket.dto.response.MemberListResponse;
import com.dorandoran.global.websocket.session.StompSessionRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class StompService {

    private final MemberService memberService;
    private final StompSessionRegistry sessionRegistry;
    private final SimpMessagingTemplate messagingTemplate;

    public void broadcastMember() {
        MemberListResponse response = getCurrentMembers();

        messagingTemplate.convertAndSend("/sub/chat/users", response);
    }

    public MemberListResponse getCurrentMembers() {
        Set<String> members = sessionRegistry.getConnectedMemberIds();

        List<Member> memberList = memberService.findAllByIds(members);

        List<ChatMemberInfo> userInfos = memberList.stream()
                .map(member -> ChatMemberInfo.of(String.valueOf(member.getId()), member.getNickname()))
                .toList();

        return new MemberListResponse(userInfos);
    }
}
