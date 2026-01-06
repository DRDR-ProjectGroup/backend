package com.dorandoran.domain.member.controller;

import com.dorandoran.SpringBootTestSupporter;
import com.dorandoran.domain.member.entity.Member;
import com.dorandoran.global.response.ErrorCode;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static com.dorandoran.global.jwt.JWTConstant.ACCESS_TOKEN_CATEGORY;
import static com.dorandoran.global.jwt.JWTConstant.REFRESH_TOKEN_CATEGORY;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
class AuthControllerTest extends SpringBootTestSupporter {

    @DisplayName("내 정보 조회")
    @Test
    void getMyInfo() throws Exception {
        // given
        Member member = memberFactory.saveAndCreateMember(1).getFirst();

        // 실제 JWT 생성 (AccessToken, RefreshToken)
        String accessToken = jwtUtil.createJwt(ACCESS_TOKEN_CATEGORY, member.getId().toString(), member.getRole().name(), jwtProperties.getAccessExpiration());
        String refreshToken = jwtUtil.createJwt(REFRESH_TOKEN_CATEGORY, member.getId().toString(), member.getRole().name(), jwtProperties.getRefreshExpiration());

        // when
        ResultActions result = mockMvc.perform(
                get("/api/v1/auth/me")
                        .with(user(String.valueOf(member.getId())).roles("MEMBER"))
                        .header("Authorization", "Bearer " + accessToken)
                        .cookie(new Cookie("RefreshToken", refreshToken))
        );

        // then
        result.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.memberId").value(member.getId().intValue()))
                .andExpect(jsonPath("$.data.username").value(member.getUsername()))
                .andExpect(jsonPath("$.data.email").value(member.getEmail()))
                .andExpect(jsonPath("$.data.nickname").value(member.getNickname()))
                .andExpect(jsonPath("$.data.accessToken").value(accessToken))
                .andExpect(jsonPath("$.data.refreshToken").value(refreshToken));
    }

    @DisplayName("내 정보 조회 - 존재하지 않는 회원")
    @Test
    void getMyInfo_NotFoundMember() throws Exception {
        // given
        Long invalidUserId = 9999L;

        String accessToken = jwtUtil.createJwt(ACCESS_TOKEN_CATEGORY, invalidUserId.toString(), "ROLE_MEMBER", jwtProperties.getAccessExpiration());
        String refreshToken = jwtUtil.createJwt(REFRESH_TOKEN_CATEGORY, invalidUserId.toString(), "ROLE_MEMBER", jwtProperties.getRefreshExpiration());

        // when
        ResultActions result = mockMvc.perform(
                get("/api/v1/auth/me")
                        .with(user(String.valueOf(invalidUserId)).roles("MEMBER"))
                        .header("Authorization", "Bearer " + accessToken)
                        .cookie(new Cookie("RefreshToken", refreshToken))
        );

        // then: GlobalExceptionHandler의 일반 Exception 핸들러가 동작해서 ErrorCode.FAIL(400)로 응답합니다.
        result.andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.FAIL.getHttpStatus().value()))
                .andExpect(jsonPath("$.message").value(ErrorCode.FAIL.getMessage()));
    }

    @DisplayName("토큰 재발급")
    @Test
    void reissueToken() throws Exception {
        // given
        Member member = memberFactory.saveAndCreateMember(1).getFirst();
        String userId = member.getId().toString();
        String role = member.getRole().name();

        String oldRefresh = jwtUtil.createJwt(REFRESH_TOKEN_CATEGORY, userId, role, jwtProperties.getRefreshExpiration());

        // Redis에 저장된 값이 oldRefresh 라고 가정
        given(redisRepository.getRefreshToken(userId)).willReturn(oldRefresh);

        // when
        ResultActions result = mockMvc.perform(
                post("/api/v1/auth/reissue")
                        .cookie(new Cookie("RefreshToken", oldRefresh))
        );

        // then
        result.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // verify saveRefreshToken 호출 (userId, newRefreshToken, expiration)
        verify(redisRepository).saveRefreshToken(eq(userId), anyString(), anyLong());
    }

    @DisplayName("토큰 재발급 - 만료된 리프레시 토큰")
    @Test
    void reissueToken_ExpiredRefreshToken() throws Exception {
        // given
        String expiredRefreshToken = jwtUtil.createJwt(REFRESH_TOKEN_CATEGORY, "1", "ROLE_MEMBER", -1000L); // 이미 만료된 토큰

        // when
        ResultActions result = mockMvc.perform(
                post("/api/v1/auth/reissue")
                        .cookie(new Cookie("RefreshToken", expiredRefreshToken))
        );

        // then
        result.andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_TOKEN.getHttpStatus().value()))
                .andExpect(jsonPath("$.message").value(ErrorCode.INVALID_TOKEN.getMessage()));
    }

    @DisplayName("토큰 재발급 - Redis에 토큰이 없는 경우")
    @Test
    void reissueToken_NoTokenInRedis() throws Exception {
        // given
        Member member = memberFactory.saveAndCreateMember(1).getFirst();
        String userId = member.getId().toString();
        String role = member.getRole().name();

        String oldRefresh = jwtUtil.createJwt(REFRESH_TOKEN_CATEGORY, userId, role, jwtProperties.getRefreshExpiration());

        given(redisRepository.getRefreshToken(userId)).willReturn(null);

        // when
        ResultActions result = mockMvc.perform(
                post("/api/v1/auth/reissue")
                        .cookie(new Cookie("RefreshToken", oldRefresh))
        );

        // then
        result.andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_TOKEN.getHttpStatus().value()))
                .andExpect(jsonPath("$.message").value(ErrorCode.INVALID_TOKEN.getMessage()));
    }

    @DisplayName("리프레시토큰 검증")
    @Test
    void validateRefreshToken() throws Exception {
        // given
        Member member = memberFactory.saveAndCreateMember(1).getFirst();
        String userId = member.getId().toString();
        String role = member.getRole().name();

        // Case 1: Redis에 저장된 토큰과 제출된 토큰 불일치 -> INVALID_TOKEN
        String submittedRefresh = jwtUtil.createJwt(REFRESH_TOKEN_CATEGORY, userId, role, jwtProperties.getRefreshExpiration());
        String savedDifferent = "different-refresh-token";

        given(redisRepository.getRefreshToken(userId)).willReturn(savedDifferent);

        ResultActions mismatchResult = mockMvc.perform(
                post("/api/v1/auth/reissue")
                        .cookie(new Cookie("RefreshToken", submittedRefresh))
        );

        mismatchResult.andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_TOKEN.getHttpStatus().value()))
                .andExpect(jsonPath("$.message").value(ErrorCode.INVALID_TOKEN.getMessage()));

        verify(redisRepository, never()).saveRefreshToken(eq(userId), anyString(), anyLong());

        // Case 2: 제출된 토큰 카테고리가 잘못된 경우 (accessToken 카테고리) -> INVALID_TOKEN
        String wrongCategoryToken = jwtUtil.createJwt(ACCESS_TOKEN_CATEGORY, userId, role, jwtProperties.getRefreshExpiration());

        given(redisRepository.getRefreshToken(userId)).willReturn(jwtUtil.createJwt(REFRESH_TOKEN_CATEGORY, userId, role, jwtProperties.getRefreshExpiration()));

        ResultActions wrongCategoryResult = mockMvc.perform(
                post("/api/v1/auth/reissue")
                        .cookie(new Cookie("RefreshToken", wrongCategoryToken))
        );

        wrongCategoryResult.andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_TOKEN.getHttpStatus().value()))
                .andExpect(jsonPath("$.message").value(ErrorCode.INVALID_TOKEN.getMessage()));
    }
}