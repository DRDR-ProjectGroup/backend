package com.dorandoran.domain.member.service;

import com.dorandoran.SpringBootTestSupporter;
import com.dorandoran.domain.member.dto.request.EmailRequest;
import com.dorandoran.domain.member.dto.request.EmailVerificationRequest;
import com.dorandoran.domain.member.dto.request.JoinRequest;
import com.dorandoran.domain.member.entity.Member;
import com.dorandoran.global.exception.CustomException;
import com.dorandoran.global.response.ErrorCode;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;

@Transactional
class MemberServiceTest extends SpringBootTestSupporter {

    @DisplayName("이메일 전송 - 이메일 중복")
    @Test
    void sendEmail_duplicateEmail() {
        Member member = memberFactory.saveAndCreateMember(1).getFirst();
        String email = member.getEmail();
        EmailRequest dto = new EmailRequest(email);

        // when / then
        assertThatThrownBy(() -> memberService.sendCodeToEmail(dto))
                .isInstanceOf(CustomException.class)
                .extracting("code")
                .isEqualTo(ErrorCode.DUPLICATED_EMAIL);
    }

    @DisplayName("이메일 전송 - 전송 실패")
    @Test
    void sendEmail_failToSend() throws MessagingException, UnsupportedEncodingException {
        // given
        EmailRequest dto = new EmailRequest("test@naver.com");

        // 이메일 전송 시 예외 발생
        doThrow(new RuntimeException("SMTP error"))
                .when(emailService)
                .sendEmail(anyString(), anyString(), anyString());

        // when / then
        assertThatThrownBy(() -> memberService.sendCodeToEmail(dto))
                .isInstanceOf(CustomException.class)
                .extracting("code")
                .isEqualTo(ErrorCode.EMAIL_SEND_FAIL);
    }

    @DisplayName("회원가입 - username 중복")
    @Test
    void validateDuplicateMember_username() {
        // given
        Member member = memberFactory.saveAndCreateMember(1).getFirst();

        JoinRequest dto = new JoinRequest(member.getUsername(), "Test@1234", "Test@1234", "nick", "test99@naver.com");
        given(redisRepository.isEmailVerified(dto.getEmail())).willReturn(true);

        // when / then
        assertThatThrownBy(() -> memberService.join(dto))
                .isInstanceOf(CustomException.class)
                .extracting("code")
                .isEqualTo(ErrorCode.DUPLICATED_USERNAME);
    }

    @DisplayName("회원가입 - nickname 중복")
    @Test
    void validateDuplicateMember_nickname() {
        // given
        Member member = memberFactory.saveAndCreateMember(1).getFirst();

        JoinRequest dto = new JoinRequest("test99", "Test@1234", "Test@1234", member.getNickname(), "test99@naver.com");
        given(redisRepository.isEmailVerified(dto.getEmail())).willReturn(true);

        // when / then
        assertThatThrownBy(() -> memberService.join(dto))
                .isInstanceOf(CustomException.class)
                .extracting("code")
                .isEqualTo(ErrorCode.DUPLICATED_NICKNAME);
    }

    @DisplayName("회원가입 - email 중복")
    @Test
    void validateDuplicateMember_email() {
        // given
        Member member = memberFactory.saveAndCreateMember(1).getFirst();

        JoinRequest dto = new JoinRequest("test99", "Test@1234", "Test@1234", "nick", member.getEmail());
        given(redisRepository.isEmailVerified(dto.getEmail())).willReturn(true);

        // when / then
        assertThatThrownBy(() -> memberService.join(dto))
                .isInstanceOf(CustomException.class)
                .extracting("code")
                .isEqualTo(ErrorCode.DUPLICATED_EMAIL);
    }

    @DisplayName("인증 코드 검증 - null")
    @Test
    void verifyCode() {
        // given
        String email = "test@naver.com";
        int code = 123456;
        EmailVerificationRequest dto = new EmailVerificationRequest(email, code);

        given(redisRepository.getAuthCode(email)).willReturn(null);

        // when / then
        assertThatThrownBy(() -> memberService.verifyEmail(dto))
                .isInstanceOf(CustomException.class)
                .extracting("code")
                .isEqualTo(ErrorCode.AUTH_CODE_NOT_FOUND);
    }

    @DisplayName("인증 코드 검증 - another code")
    @Test
    void verifyCode_another() {
        // given
        String email = "test@naver.com";
        int code = 123456;
        EmailVerificationRequest dto = new EmailVerificationRequest(email, code);

        given(redisRepository.getAuthCode(email)).willReturn("654321");

        // when / then
        assertThatThrownBy(() -> memberService.verifyEmail(dto))
                .isInstanceOf(CustomException.class)
                .extracting("code")
                .isEqualTo(ErrorCode.INVALID_AUTH_CODE);
    }
}