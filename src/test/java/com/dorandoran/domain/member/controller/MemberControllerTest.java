package com.dorandoran.domain.member.controller;

import com.dorandoran.SpringBootTestSupporter;
import com.dorandoran.domain.member.dto.request.EmailRequest;
import com.dorandoran.domain.member.dto.request.EmailVerificationRequest;
import com.dorandoran.domain.member.dto.request.JoinRequest;
import com.dorandoran.domain.member.dto.request.LoginRequest;
import com.dorandoran.domain.member.entity.Member;
import com.dorandoran.factory.MemberFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static com.dorandoran.global.response.ErrorCode.EMAIL_NOT_VERIFIED;
import static com.dorandoran.global.response.SuccessCode.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
class MemberControllerTest extends SpringBootTestSupporter {

    @DisplayName("join 테스트")
    @Test
    void join() throws Exception {
        // given
        String username = "test";
        String password = "test@1234";
        String email = "test@naver.com";
        String nickname = "tester";

        JoinRequest joinRequest = new JoinRequest(
                username,
                password,
                password,
                nickname,
                email
        );

        // RedisRepository는 @MockitoBean 으로 주입된 모의 객체이므로 이메일 인증을 통과하도록 설정
        given(redisRepository.isEmailVerified(email)).willReturn(true);

        // when
        ResultActions result = mockMvc.perform(post("/api/v1/members/join")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(joinRequest)));


        // then
        result.andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value(JOIN_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.code").value(JOIN_SUCCESS.getHttpStatus().value()));
    }

    @DisplayName("join 실패 테스트 - 이메일 인증 안된 경우")
    @Test
    void joinFailed() throws Exception {
        // given
        String username = "test";
        String password = "test@1234";
        String email = "test@naver.com";
        String nickname = "tester";

        JoinRequest joinRequest = new JoinRequest(
                username,
                password,
                password,
                nickname,
                email
        );

        // when
        ResultActions result = mockMvc.perform(post("/api/v1/members/join")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(joinRequest)));


        // then
        result.andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(EMAIL_NOT_VERIFIED.getMessage()))
                .andExpect(jsonPath("$.code").value(EMAIL_NOT_VERIFIED.getHttpStatus().value()));
    }

    @DisplayName("sendEmail 테스트")
    @Test
    void sendEmail() throws Exception {
        // given
        String email = "test@naver.com";
        EmailRequest emailRequest = new EmailRequest(email);

        // when
        ResultActions result = mockMvc.perform(post("/api/v1/members/sendEmail")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(emailRequest)));

        // then
        result.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(EMAIL_SEND_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.code").value(EMAIL_SEND_SUCCESS.getHttpStatus().value()));
    }

    @DisplayName("verifyEmail 테스트")
    @Test
    void verifyEmail() throws Exception {
        // given
        String email = "test@naver.com";
        int code = 123456;
        EmailVerificationRequest request = new EmailVerificationRequest(email, code);

        given(redisRepository.getAuthCode(email)).willReturn(String.valueOf(code));

        // when
        ResultActions result = mockMvc.perform(post("/api/v1/members/verifyEmail")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(EMAIL_VERIFY_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.code").value(EMAIL_VERIFY_SUCCESS.getHttpStatus().value()));
    }

    @DisplayName("login 테스트")
    @Test
    void login() throws Exception {
        // given
        Member member = memberFactory.saveAndCreateMember(1).getFirst();
        String username = member.getUsername();
        String password = MemberFactory.MEMBER_PW;
        LoginRequest loginRequest = new LoginRequest(username, password);

        // when
        ResultActions result = mockMvc.perform(post("/api/v1/members/login")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(loginRequest)));

        // then
        result.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(LOGIN_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.code").value(LOGIN_SUCCESS.getHttpStatus().value()));
    }

    @DisplayName("logout 테스트")
    @Test
    void logout() throws Exception {
        // given
        Member member = memberFactory.saveAndCreateMember(1).getFirst();

        // when
        ResultActions result = mockMvc.perform(post("/api/v1/members/logout")
                .with(user(member.getUsername()).roles("MEMBER"))
                .contentType("application/json"));

        // then
        result.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(LOGOUT_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.code").value(LOGOUT_SUCCESS.getHttpStatus().value()));

        // RedisRepository mock에 대해 deleteRefreshToken이 호출됐는지 검증
        verify(redisRepository).deleteRefreshToken(member.getUsername());
    }
}