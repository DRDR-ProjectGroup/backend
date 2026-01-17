package com.dorandoran.domain.message.controller;

import com.dorandoran.SpringBootTestSupporter;
import com.dorandoran.domain.member.entity.Member;
import com.dorandoran.domain.message.dto.request.MessageSendRequest;
import com.dorandoran.domain.message.entity.Message;
import com.dorandoran.global.response.SuccessCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
class MessageControllerTest extends SpringBootTestSupporter {

    private List<Member> memberList;
    private Member member;

    @BeforeEach
    void setUp() {
        memberList = memberFactory.saveAndCreateMember(10);
        member = memberList.getFirst();
    }

    @DisplayName("메세지 전송 테스트")
    @Test
    void sendMessage() throws Exception {
        // given
        Long receiverId = memberList.get(1).getId();
        MessageSendRequest request = new MessageSendRequest("message content");

        // when
        ResultActions result = mockMvc.perform(post("/api/v1/messages/{receiverId}", receiverId)
                .with(user(String.valueOf(member.getId())))
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request))
        );

        // then
        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value(SuccessCode.MESSAGE_SEND_SUCCESS.getMessage()));
    }

    @DisplayName("메세지 조회 테스트 - 단건")
    @Test
    void getMessage() throws Exception {
        // given
        String content = "message content";
        Message message = messageFactory.saveAndCreateMessage(member, memberList.get(1), content);

        // when
        ResultActions result = mockMvc.perform(get("/api/v1/messages/{messageId}", message.getId())
                .with(user(String.valueOf(member.getId())))
        );

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.messageId").value(message.getId()))
                .andExpect(jsonPath("$.data.content").value(content));
    }

    @DisplayName("보낸 메세지 조회 테스트 - 다건")
    @Test
    void getMessagesSent() throws Exception {
        // given
        String content = "message content";
        Message message = messageFactory.saveAndCreateMessage(member, memberList.get(1), content);
        Message message2 = messageFactory.saveAndCreateMessage(memberList.get(1), member, content);

        // when
        ResultActions result = mockMvc.perform(get("/api/v1/messages")
                .with(user(String.valueOf(member.getId())))
                .param("type", "sent")
                .param("page", "1")
                .param("size", "20")
        );

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.messages[0].messageId").value(message.getId()))
                .andExpect(jsonPath("$.data.messages[0].content").value(content));
    }

    @DisplayName("받은 메세지 조회 테스트 - 다건")
    @Test
    void getMessagesInbox() throws Exception {
        // given
        String content = "message content";
        Message message = messageFactory.saveAndCreateMessage(member, memberList.get(1), content);
        Message message2 = messageFactory.saveAndCreateMessage(memberList.get(1), member, content);

        // when
        ResultActions result = mockMvc.perform(get("/api/v1/messages")
                .with(user(String.valueOf(member.getId())))
                .param("type", "inbox")
                .param("page", "1")
                .param("size", "20")
        );

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.messages[0].messageId").value(message2.getId()))
                .andExpect(jsonPath("$.data.messages[0].content").value(content));
    }

    @DisplayName("메세지 삭제 테스트 - 보낸편지")
    @Test
    void deleteSendMessage() throws Exception {
        // given
        String content = "message content";
        Message message = messageFactory.saveAndCreateMessage(member, memberList.get(1), content);

        // when
        ResultActions result = mockMvc.perform(delete("/api/v1/messages/{messageId}", message.getId())
                .with(user(String.valueOf(member.getId())))
        );

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(SuccessCode.MESSAGE_DELETE_SUCCESS.getMessage()));
    }

    @DisplayName("메세지 삭제 테스트 - 보낸편지")
    @Test
    void deleteReceiveMessage() throws Exception {
        // given
        String content = "message content";
        Message message = messageFactory.saveAndCreateMessage(memberList.get(1), member, content);

        // when
        ResultActions result = mockMvc.perform(delete("/api/v1/messages/{messageId}", message.getId())
                .with(user(String.valueOf(member.getId())))
        );

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(SuccessCode.MESSAGE_DELETE_SUCCESS.getMessage()));
    }
}