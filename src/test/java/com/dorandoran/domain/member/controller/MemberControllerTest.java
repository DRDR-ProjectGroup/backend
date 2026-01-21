package com.dorandoran.domain.member.controller;

import com.dorandoran.SpringBootTestSupporter;
import com.dorandoran.domain.member.dto.request.*;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

    @DisplayName("resign 테스트")
    @Test
    void resign() throws Exception {
        // given
        Member member = memberFactory.saveAndCreateMember(1).getFirst();

        // when
        ResultActions result = mockMvc.perform(delete("/api/v1/members/resign")
                .with(user(String.valueOf(member.getId())).roles("MEMBER"))
                .contentType("application/json"));

        // then
        result.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(RESIGN_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.code").value(RESIGN_SUCCESS.getHttpStatus().value()));

        // RedisRepository mock에 대해 deleteRefreshToken이 호출됐는지 검증
        verify(redisRepository).deleteRefreshToken(String.valueOf(member.getId()));
    }

    @DisplayName("getMemberInfo 테스트")
    @Test
    void getMemberInfo() throws Exception {
        // given
        Member member = memberFactory.saveAndCreateMember(1).getFirst();

        // when
        ResultActions result = mockMvc.perform(get("/api/v1/members/me")
                .with(user(String.valueOf(member.getId())).roles("MEMBER"))
                .contentType("application/json"));

        // then
        result.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(MEMBER_INFO_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.code").value(MEMBER_INFO_SUCCESS.getHttpStatus().value()))
                .andExpect(jsonPath("$.data.username").value(member.getUsername()))
                .andExpect(jsonPath("$.data.nickname").value(member.getNickname()))
                .andExpect(jsonPath("$.data.email").value(member.getEmail()));
    }

    @DisplayName("modifyNickname 테스트")
    @Test
    void modifyNickname() throws Exception {
        // given
        // given
        Member member = memberFactory.saveAndCreateMember(1).getFirst();
        NicknameRequest nicknameRequest = new NicknameRequest("newNick");

        // when
        ResultActions result = mockMvc.perform(patch("/api/v1/members/me/nickname")
                .with(user(String.valueOf(member.getId())).roles("MEMBER"))
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(nicknameRequest)));

        // then
        result.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(NICKNAME_MODIFY_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.code").value(NICKNAME_MODIFY_SUCCESS.getHttpStatus().value()));
    }

    @DisplayName("modifyPassword 테스트")
    @Test
    void modifyPassword() throws Exception {
        // given
        Member member = memberFactory.saveAndCreateMember(1).getFirst();
        PasswordRequest passwordRequest = new PasswordRequest("test@1234", "NewPass@1234", "NewPass@1234");

        // when
        ResultActions result = mockMvc.perform(patch("/api/v1/members/me/password")
                .with(user(String.valueOf(member.getId())).roles("MEMBER"))
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(passwordRequest)));

        // then
        result.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(PASSWORD_MODIFY_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.code").value(PASSWORD_MODIFY_SUCCESS.getHttpStatus().value()));
    }

    @DisplayName("마이페이지 - 내가 쓴 글 조회")
    @Test
    void getMyPosts() throws Exception {
        // given
        Member member = memberFactory.saveAndCreateMember(1).getFirst();

        // when
        ResultActions result = mockMvc.perform(get("/api/v1/members/me/posts")
                        .with(user(String.valueOf(member.getId())).roles("MEMBER")))
                .andDo(print());

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(MY_POSTS_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.code").value(MY_POSTS_SUCCESS.getHttpStatus().value()));
    }

    @DisplayName("마이페이지 - 내가 쓴 댓글 조회")
    @Test
    void getMyComments() throws Exception {
        // given
        Member member = memberFactory.saveAndCreateMember(1).getFirst();

        // when
        ResultActions result = mockMvc.perform(get("/api/v1/members/me/comments")
                        .with(user(String.valueOf(member.getId())).roles("MEMBER")))
                .andDo(print());

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(MY_COMMENTS_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.code").value(MY_COMMENTS_SUCCESS.getHttpStatus().value()));
    }
}