package com.dorandoran.domain.member.controller;

import com.dorandoran.domain.member.dto.response.AuthMemberResponse;
import com.dorandoran.domain.member.entity.Member;
import com.dorandoran.domain.member.repository.MemberRepository;
import com.dorandoran.global.jwt.JWTUtil;
import com.dorandoran.global.jwt.JwtProperties;
import com.dorandoran.global.redis.RedisRepository;
import com.dorandoran.global.response.BaseResponse;
import com.dorandoran.global.response.SuccessCode;
import com.dorandoran.global.security.filter.FilterUtil;
import com.dorandoran.standard.util.ControllerUt;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

import static com.dorandoran.global.jwt.JWTConstant.*;
import static com.dorandoran.global.response.ErrorCode.INVALID_TOKEN;
import static com.dorandoran.global.response.SuccessCode.TOKEN_REISSUE_SUCCESS;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Tag(name = "AuthenticationController", description = "회원 인증 API")
@Slf4j
public class AuthController {

    private final JWTUtil jwtUtil;
    private final RedisRepository redisRepository;
    private final JwtProperties jwtProperties;
    private final MemberRepository memberRepository;

    @GetMapping("/me")
    @Operation(summary = "내 정보 조회(개발용)", description = "현재 인증된 사용자의 정보를 조회합니다.")
    @SecurityRequirement(name = "bearerAuth")
    public BaseResponse<AuthMemberResponse> getMyInfo(HttpServletRequest request, Principal principal) {
        Long userId = Long.parseLong(principal.getName());
        Member findMember = memberRepository.findById(userId).orElseThrow(
                () -> new JwtException("존재하지 않는 회원입니다.")
        );
        String accesstoken = FilterUtil.extractAccessToken(request);
        String refreshtoken = FilterUtil.extractRefreshToken(request);
        return BaseResponse.ok(SuccessCode.SUCCESS, AuthMemberResponse.of(findMember, accesstoken, refreshtoken));
    }

    @PostMapping("/reissue")
    @Operation(summary = "토큰 재발급", description = "만료된 AccessToken을 RefreshToken으로 재발급합니다.")
    @SecurityRequirement(name = "bearerAuth")
    public BaseResponse<Void> reissueToken(HttpServletRequest request, HttpServletResponse response) {
        // AccessToken이 만료되었을 때, RefreshToken으로 AccessToken과 RefreshToken을 재발급 해줍니다.
        // 재발급 범위 : RefreshToken, AccessToken
        // 1. RefreshToken Cookie에서 추출
        String refreshToken = FilterUtil.extractRefreshToken(request);

        // 2. RefreshToken 검증, 만료되었다면 재발급 불가 (로그아웃 처리)
        if (refreshToken == null || isExpired(refreshToken)) {
            return BaseResponse.fail(INVALID_TOKEN);
        }

        String userId;
        String role;

        try {
            // 3. RefreshToken으로부터 MemberId, role 추출
            userId = jwtUtil.getUserId(refreshToken);
            role = jwtUtil.getRole(refreshToken);

            // 4. Redis에서 저장된 RefreshToken 조회, 없으면 재발급 불가 (로그아웃 처리)
            String savedRefreshToken = redisRepository.getRefreshToken(userId);
            if (savedRefreshToken == null) {
                log.error("Redis 에 저장된 refresh Token 이 존재하지 않음.");
                return BaseResponse.fail(INVALID_TOKEN);
            }

            // 5. RefreshToken 일치 여부 검증, 예외 시 재발급 불가 (로그아웃 처리)
            validateRefreshToken(refreshToken, savedRefreshToken);

        } catch (JwtException e) {
            log.warn("Invalid refresh token: {}", e.getMessage());
            return BaseResponse.fail(INVALID_TOKEN);
        }

        // 6. 새로운 AccessToken, RefreshToken 생성
        String newAccessToken = jwtUtil.createJwt(ACCESS_TOKEN_CATEGORY, userId, role, jwtProperties.getAccessExpiration());
        String newRefreshToken = jwtUtil.createJwt(REFRESH_TOKEN_CATEGORY, userId, role, jwtProperties.getRefreshExpiration());

        // 7. Redis에 새로운 RefreshToken 저장
        redisRepository.saveRefreshToken(userId, newRefreshToken, jwtProperties.getRefreshExpiration());

        // 8. Response Header와 Cookie에 새로운 토큰들 세팅
        ControllerUt.addHeaderResponse(
                ACCESS_TOKEN_HEADER,
                ControllerUt.makeBearerToken(newAccessToken),
                response);

        ControllerUt.addCookie(
                REFRESH_TOKEN_HEADER,
                newRefreshToken,
                ControllerUt.parseMsToSec(jwtProperties.getRefreshExpiration()),
                response);

        return BaseResponse.ok(TOKEN_REISSUE_SUCCESS);
    }

    private void validateRefreshToken(String refreshToken, String savedRefreshToken) {
        // 1. refresh Token Category 검증
        String category = jwtUtil.getCategory(refreshToken);
        if (!REFRESH_TOKEN_CATEGORY.equals(category)) {
            throw new JwtException("Invalid refresh token category");
        }

        // 2. 저장된 refreshToken 과 동일한지 확인
        if (!refreshToken.equals(savedRefreshToken)) {
            log.error("저장된 refresh Token 과 전달받은 refresh token 이 일치하지 않음.");
            throw new JwtException("Invalid refresh token");
        }
    }

    private boolean isExpired(String token) {
        try {
            jwtUtil.isExpired(token);
        } catch (ExpiredJwtException e) {
            return true;
        }
        return false;
    }
}
