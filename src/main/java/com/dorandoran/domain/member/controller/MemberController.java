package com.dorandoran.domain.member.controller;

import com.dorandoran.domain.comment.dto.response.CommentListMemberResponse;
import com.dorandoran.domain.member.dto.request.*;
import com.dorandoran.domain.member.dto.response.MemberInfoResponse;
import com.dorandoran.domain.member.dto.response.MemberTokenResponse;
import com.dorandoran.domain.member.service.MemberService;
import com.dorandoran.domain.post.dto.response.PostListResponse;
import com.dorandoran.global.jwt.JwtProperties;
import com.dorandoran.global.response.BaseResponse;
import com.dorandoran.global.response.SuccessCode;
import com.dorandoran.standard.page.dto.PageCommentDto;
import com.dorandoran.standard.page.dto.PageDto;
import com.dorandoran.standard.util.ControllerUt;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

import static com.dorandoran.global.jwt.JWTConstant.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members")
@Tag(name = "MemberController", description = "회원 관련 API")
@Slf4j
public class MemberController {

    private final MemberService memberService;
    private final JwtProperties jwtProperties;

    @PostMapping("/join")
    @Operation(summary = "일반 회원 가입")
    public BaseResponse<Void> join(
            @Valid @RequestBody JoinRequest joinDto
    ) {
        memberService.join(joinDto);
        return BaseResponse.ok(SuccessCode.JOIN_SUCCESS);
    }

    @PostMapping("/sendEmail")
    @Operation(summary = "이메일 인증 코드 전송")
    public BaseResponse<Void> sendEmail(
            @Valid @RequestBody EmailRequest emailDto
    ) {
        memberService.sendCodeToEmail(emailDto);
        return BaseResponse.ok(SuccessCode.EMAIL_SEND_SUCCESS);
    }

    @PostMapping("/verifyEmail")
    @Operation(summary = "이메일 인증 코드 검증")
    public BaseResponse<Void> verifyEmail(
            @Valid @RequestBody EmailVerificationRequest dto
    ) {
        memberService.verifyEmail(dto);
        return BaseResponse.ok(SuccessCode.EMAIL_VERIFY_SUCCESS);
    }

    @PostMapping("/login")
    @Operation(summary = "로그인")
    public BaseResponse<Void> login(
            @Valid @RequestBody LoginRequest dto,
            HttpServletResponse response
    ) {
        log.info("Login User: {}", dto.getUsername());
        MemberTokenResponse token = memberService.login(dto);
        addJwtTokenResponse(response, token);
        return BaseResponse.ok(SuccessCode.LOGIN_SUCCESS);
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃")
    @SecurityRequirement(name = "bearerAuth")   // Swagger 에서 Bearer 인증 필요함을 명시
    public BaseResponse<Void> logout(
            HttpServletResponse response,
            Principal principal
    ) {
        log.info("Logout User: {}", principal.getName());
        // memberService.logout 에서 Redis 에서 refresh token 삭제
        memberService.logout(principal.getName());

        // 클라이언트 쿠키에서 refresh token 삭제
        deleteRefreshTokenCookie(response);
        return BaseResponse.ok(SuccessCode.LOGOUT_SUCCESS);
    }

    @DeleteMapping("/resign")
    @Operation(summary = "회원 탈퇴")
    @SecurityRequirement(name = "bearerAuth")
    public BaseResponse<Void> resign(
            HttpServletResponse response,
            Principal principal
    ) {
        log.info("Resign User: {}", principal.getName());
        memberService.resign(principal.getName());
        deleteRefreshTokenCookie(response);
        return BaseResponse.ok(SuccessCode.RESIGN_SUCCESS);
    }

    @GetMapping("/me")
    @Operation(summary = "회원 정보 조회")
    @SecurityRequirement(name = "bearerAuth")
    public BaseResponse<MemberInfoResponse> getMemberInfo(
            Principal principal
    ) {
        MemberInfoResponse memberInfo = memberService.getMemberInfo(principal.getName());
        return BaseResponse.ok(SuccessCode.MEMBER_INFO_SUCCESS, memberInfo);
    }

    @PatchMapping("/me/nickname")
    @Operation(summary = "닉네임 수정")
    @SecurityRequirement(name = "bearerAuth")
    public BaseResponse<Void> modifyNickname(
            @Valid @RequestBody NicknameRequest nicknameDto,
            Principal principal
    ) {
        log.info("Modify Nickname User: {}", principal.getName());
        memberService.modifyNickname(principal.getName(), nicknameDto);
        return BaseResponse.ok(SuccessCode.NICKNAME_MODIFY_SUCCESS);
    }

    @PatchMapping("/me/password")
    @Operation(summary = "비밀번호 수정")
    @SecurityRequirement(name = "bearerAuth")
    public BaseResponse<Void> modifyPassword(
            @Valid @RequestBody PasswordRequest passwordDto,
            Principal principal
    ) {
        log.info("Modify Password User: {}", principal.getName());
        memberService.modifyPassword(principal.getName(), passwordDto);
        return BaseResponse.ok(SuccessCode.PASSWORD_MODIFY_SUCCESS);
    }

    private void addJwtTokenResponse(HttpServletResponse response, MemberTokenResponse token) {
        ControllerUt.addHeaderResponse(
                ACCESS_TOKEN_HEADER,
                ControllerUt.makeBearerToken(token.getAccessToken()),
                response);
        ControllerUt.addCookie(
                REFRESH_TOKEN_HEADER,
                token.getRefreshToken(),
                (int) jwtProperties.getRefreshExpiration(),
                response);
        ControllerUt.addCookie(
                GUEST_TOKEN_HEADER,
                null,
                0,
                response
        );
    }

    private void deleteRefreshTokenCookie(HttpServletResponse response) {
        ControllerUt.addCookie(
                REFRESH_TOKEN_HEADER,
                null,
                0,
                response);
    }

    @GetMapping("/me/posts")
    @Operation(summary = "내가 작성한 게시글 조회")
    @SecurityRequirement(name = "bearerAuth")
    public BaseResponse<PageDto<PostListResponse>> getMyPosts(
            Principal principal,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageDto<PostListResponse> myPosts = memberService.getMyPosts(principal.getName(), page, size);
        return BaseResponse.ok(SuccessCode.MY_POSTS_SUCCESS, myPosts);
    }

    @GetMapping("/me/comments")
    @Operation(summary = "내가 작성한 댓글 조회")
    @SecurityRequirement(name = "bearerAuth")
    public BaseResponse<PageCommentDto<CommentListMemberResponse>> getMyComments(
            Principal principal,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageCommentDto<CommentListMemberResponse> myComments = memberService.getMyComments(principal.getName(), page, size);
        return BaseResponse.ok(SuccessCode.MY_COMMENTS_SUCCESS, myComments);
    }

}
