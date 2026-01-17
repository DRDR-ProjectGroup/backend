package com.dorandoran.domain.message.service;

import com.dorandoran.SpringBootTestSupporter;
import com.dorandoran.domain.member.entity.Member;
import com.dorandoran.domain.message.dto.request.MessageSendRequest;
import com.dorandoran.domain.message.entity.Message;
import com.dorandoran.global.response.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
class MessageServiceTest extends SpringBootTestSupporter {

    private List<Member> memberList;
    private Member member;

    @BeforeEach
    void setUp() {
        memberList = memberFactory.saveAndCreateMember(10);
        member = memberList.getFirst();
    }

    @DisplayName("메세지 전송 실패 - 자신에게 보낼 때")
    @Test
    void sendMessageToMe() {
        // given
        Long receiverId = member.getId();
        MessageSendRequest request = new MessageSendRequest("message content");

        // when & then
        assertThatThrownBy(() -> messageService.sendMessage(receiverId, String.valueOf(member.getId()), request))
                .isInstanceOf(Exception.class)
                .extracting("code")
                .isEqualTo(ErrorCode.CANNOT_SEND_MESSAGE_TO_SELF);
    }

    @DisplayName("메세지 단건 조회 실패 - 본인과 관련 없는 메세지 조회 시도")
    @Test
    void getMessageNoAccess() {
        // given
        Message message = messageFactory.saveAndCreateMessage(
                memberList.get(1),
                memberList.get(2),
                "message content"
        );

        // when & then
        assertThatThrownBy(() -> messageService.getMessage(message.getId(), String.valueOf(member.getId())))
                .isInstanceOf(Exception.class)
                .extracting("code")
                .isEqualTo(ErrorCode.NO_ACCESS_TO_MESSAGE);
    }

    @DisplayName("메세지 단건 조회 실패 - 삭제된 메세지 조회 시도")
    @Test
    void getMessageDeleted() {
        // given
        Message message = messageFactory.saveAndCreateMessage(
                member,
                memberList.get(1),
                "message content"
        );
        messageService.deleteMessage(message.getId(), String.valueOf(member.getId()));

        // when & then
        assertThatThrownBy(() -> messageService.getMessage(message.getId(), String.valueOf(member.getId())))
                .isInstanceOf(Exception.class)
                .extracting("code")
                .isEqualTo(ErrorCode.MESSAGE_NOT_FOUND);
    }

    @DisplayName("메세지 삭제 - 권한 없음")
    @Test
    void deleteMessageNoAccess() {
        // given
        Message message = messageFactory.saveAndCreateMessage(
                memberList.get(1),
                memberList.get(2),
                "message content"
        );

        // when & then
        assertThatThrownBy(() -> messageService.deleteMessage(message.getId(), String.valueOf(member.getId())))
                .isInstanceOf(Exception.class)
                .extracting("code")
                .isEqualTo(ErrorCode.NO_ACCESS_TO_MESSAGE);
    }
}