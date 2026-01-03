package com.dorandoran.domain.member.service;

import com.dorandoran.domain.member.dto.request.LoginRequest;
import com.dorandoran.domain.member.dto.request.SignupRequest;
import com.dorandoran.domain.member.dto.response.MemberTokenResponse;
import com.dorandoran.domain.member.repository.MemberRepository;
import com.dorandoran.global.jwt.JWTUtil;
import com.dorandoran.global.jwt.JwtProperties;
import com.dorandoran.global.redis.RedisRepository;
import com.dorandoran.global.request.Rq;
import com.dorandoran.global.security.auth.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.dorandoran.global.jwt.JWTConstant.ACCESS_TOKEN_CATEGORY;
import static com.dorandoran.global.jwt.JWTConstant.REFRESH_TOKEN_CATEGORY;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;
    private final JwtProperties jwtProperties;
    private final RedisRepository redisRepository;
    private final Rq rq;

    @Transactional
    public void signup(SignupRequest signupDto) {
        // TODO: 회원 가입 로직 구현
    }

    @Transactional(readOnly = true)
    public MemberTokenResponse login(LoginRequest dto) {
        Authentication authentication = authenticateMember(dto);

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        return generateUserTokens(
                authentication.getAuthorities().iterator().next().getAuthority(),
                userDetails.getUsername());
    }

    @Transactional
    public void logout(String userId) {
        redisRepository.deleteRefreshToken(userId);
    }

    @Transactional
    public void resign(String userId) {
        // TODO: 회원 탈퇴 로직 구현
    }

    private Authentication authenticateMember(LoginRequest dto) {
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword());
        return authenticationManager.authenticate(authToken);
    }

    private MemberTokenResponse generateUserTokens(String role, String userId) {
        String access = jwtUtil.createJwt(ACCESS_TOKEN_CATEGORY, userId, role,
                jwtProperties.getAccessExpiration());
        String refresh = jwtUtil.createJwt(REFRESH_TOKEN_CATEGORY, userId, role,
                jwtProperties.getRefreshExpiration());

        saveRefreshTokenRedis(userId, refresh, jwtProperties.getRefreshExpiration());

        return MemberTokenResponse.of(access, refresh);
    }

    private void saveRefreshTokenRedis(String userId, String refreshToken, Long expirationMs) {
        redisRepository.saveRefreshToken(userId, refreshToken, expirationMs);
    }
}
