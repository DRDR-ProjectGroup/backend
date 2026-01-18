package com.dorandoran.domain.member.service;

import com.dorandoran.domain.comment.dto.response.CommentListMemberResponse;
import com.dorandoran.domain.comment.repository.CommentRepository;
import com.dorandoran.domain.member.dto.request.*;
import com.dorandoran.domain.member.dto.response.MemberDetailResponse;
import com.dorandoran.domain.member.dto.response.MemberInfoResponse;
import com.dorandoran.domain.member.dto.response.MemberTokenResponse;
import com.dorandoran.domain.member.entity.Member;
import com.dorandoran.domain.member.repository.MemberRepository;
import com.dorandoran.domain.member.type.MemberStatus;
import com.dorandoran.domain.post.dto.response.PostListResponse;
import com.dorandoran.domain.post.repository.PostRepository;
import com.dorandoran.global.exception.CustomException;
import com.dorandoran.global.jwt.JWTUtil;
import com.dorandoran.global.jwt.JwtProperties;
import com.dorandoran.global.redis.RedisRepository;
import com.dorandoran.global.response.ErrorCode;
import com.dorandoran.global.security.auth.CustomUserDetails;
import com.dorandoran.standard.page.dto.PageCommentDto;
import com.dorandoran.standard.page.dto.PageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static com.dorandoran.global.jwt.JWTConstant.ACCESS_TOKEN_CATEGORY;
import static com.dorandoran.global.jwt.JWTConstant.REFRESH_TOKEN_CATEGORY;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {

    private final MemberRepository memberRepository;
    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;
    private final JwtProperties jwtProperties;
    private final RedisRepository redisRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    @Transactional
    public void join(JoinRequest joinDto) {
        // 이메일 인증 여부 확인
        if (!redisRepository.isEmailVerified(joinDto.getEmail())) {
            throw new CustomException(ErrorCode.EMAIL_NOT_VERIFIED);
        }

        // username, nickname, email 중복 검사
        validateDuplicateMember(joinDto);

        // 회원 가입 처리
        Member newMember = Member.createMember(
                joinDto.getUsername(),
                passwordEncoder.encode(joinDto.getPassword()),
                joinDto.getEmail(),
                joinDto.getNickname()
        );

        memberRepository.save(newMember);

        redisRepository.deleteEmailVerified(joinDto.getEmail());
    }

    @Transactional
    public void sendCodeToEmail(EmailRequest emailDto) {
        String email = emailDto.getEmail();

        // 1. 이메일 중복 확인
        if (memberRepository.existsByEmail(email)) {
            throw new CustomException(ErrorCode.DUPLICATED_EMAIL);
        }

        // 2. 인증 코드 생성
        String authCode = String.valueOf(generateAuthCode());

        // 3. Redis에 인증 코드, 만료시간 저장
        saveAuthCodeRedis(email, authCode);

        // 4. 이메일 내용 작성
        String title = "[도란도란] 이메일 인증 코드 안내";

        String content = """
                <html>
                <body>
                <h2>안녕하세요. 도란도란 회원가입을 위한 이메일 인증 코드 안내입니다.</h2>
                <p>아래 인증 코드를 회원가입 화면에 입력해 주세요.</p>
                <h3 style='color: blue;'>인증 코드: %s</h3>
                <footer style='margin-top: 20px; font-size: small; color: gray;'>
                <p>이 메일은 발신 전용입니다. 본 메일에 회신하지 마시기 바랍니다.</p>
                </footer>
                </body>
                </html>
                """.formatted(authCode);

        // 5. 이메일 전송
        try {
            emailService.sendEmail(email, title, content);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", email, e.getMessage());
            throw new CustomException(ErrorCode.EMAIL_SEND_FAIL);
        }
    }

    @Transactional
    public void verifyEmail(EmailVerificationRequest dto) {
        String email = dto.getEmail();
        int code = dto.getCode();

        // Redis 에서 인증 코드 검증
        verifyAuthCodeRedis(email, code);

        // 인증 성공 시 Redis 에 이메일 인증 완료 상태 저장
        redisRepository.saveEmailVerified(email);

        // 인증 성공 시 Redis 에서 인증 코드 삭제
        redisRepository.deleteAuthCode(email);
    }

    @Transactional(readOnly = true)
    public MemberTokenResponse login(LoginRequest dto) {
        Member member = memberRepository.findByUsername(dto.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        if (member.getStatus() == MemberStatus.DELETED) {
            throw new CustomException(ErrorCode.MEMBER_DELETED);
        } else if (member.getStatus() == MemberStatus.BLOCKED) {
            throw new CustomException(ErrorCode.MEMBER_BLOCKED);
        }

        Authentication authentication = authenticateMember(dto);

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        return generateMemberTokens(
                authentication.getAuthorities().iterator().next().getAuthority(),
                userDetails.getUsername());
    }

    @Transactional
    public void logout(String userId) {
        redisRepository.deleteRefreshToken(userId);
    }

    @Transactional
    public void resign(String userId) {
        long id = Long.parseLong(userId);
        Member findMember = memberRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        redisRepository.deleteRefreshToken(userId);
        memberRepository.delete(findMember);
    }

    @Transactional(readOnly = true)
    public MemberInfoResponse getMemberInfo(String userId) {
        long id = Long.parseLong(userId);
        Member findMember = memberRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        return MemberInfoResponse.of(findMember);
    }

    @Transactional
    public void modifyNickname(String userId, NicknameRequest nicknameDto) {
        long id = Long.parseLong(userId);
        Member findMember = memberRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // 닉네임 중복 검사
        if (memberRepository.existsByNickname(nicknameDto.getNewNickname())) {
            throw new CustomException(ErrorCode.DUPLICATED_NICKNAME);
        }

        findMember.modifyNickname(nicknameDto.getNewNickname());
    }

    @Transactional
    public void modifyPassword(String userId, PasswordRequest passwordDto) {
        long id = Long.parseLong(userId);
        Member findMember = memberRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // 현재 비밀번호 일치 여부 확인
        if (passwordEncoder.matches(passwordDto.getNewPassword(), findMember.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_CURRENT_PASSWORD);
        }

        // 새 비밀번호 암호화 후 저장
        String newPassword = passwordEncoder.encode(passwordDto.getNewPassword());
        findMember.modifyPassword(newPassword);
    }

    // 회원 삭제
    @Transactional
    public void deleteExpiredMember() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(30);

        memberRepository.deleteAllByStatusDeletedAndBefore(MemberStatus.DELETED, threshold);
    }

    // 관리자 계정 생성
    @Value("${custom.admin.username}")
    private String adminUsername;

    @Value("${custom.admin.password}")
    private String adminPassword;

    @Transactional
    public void createAdminMember() {
        if (memberRepository.findByUsername(adminUsername).isPresent()) {
            return;
        }

        String username = adminUsername;
        String password = passwordEncoder.encode(adminPassword);
        String email = adminUsername + "@naver.com";
        String nickname = "관리자";

        Member member = Member.createMember(username, password, email, nickname);
        member.setRoleAdmin();

        memberRepository.save(member);
    }

    @Transactional(readOnly = true)
    public PageDto<PostListResponse> getMyPosts(String memberId, int page, int size) {
        long id = Long.parseLong(memberId);
        Member findMember = memberRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Pageable pageable = PageRequest.of(Math.max(0, page - 1), size, Sort.by(Sort.Order.desc("createdAt")));

        Page<PostListResponse> postListResponses = postRepository.findAllByMember(findMember, pageable).map(PostListResponse::of);

        return new PageDto<>(postListResponses, null);
    }

    @Transactional(readOnly = true)
    public PageCommentDto<CommentListMemberResponse> getMyComments(String memberId, int page, int size) {
        long id = Long.parseLong(memberId);
        Member findMember = memberRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Pageable pageable = PageRequest.of(Math.max(0, page - 1), size, Sort.by(Sort.Order.desc("createdAt")));

        Page<CommentListMemberResponse> commentPage = commentRepository.findAllByMember(findMember, pageable).map(CommentListMemberResponse::of);

        return new PageCommentDto<>(commentPage);
    }

    @Transactional
    public List<MemberDetailResponse> getAllMembers(String memberId) {
        Member member = findMemberByStringId(memberId);

        if (!member.isAdmin()) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        List<Member> members = memberRepository.findAll();

        return members.stream().map(MemberDetailResponse::of).toList();
    }

    @Transactional
    public void setMemberStatus(Long targetMemberId, MemberStatusRequest request, String memberId) {
        // 관리자여도 탈퇴처리는 불가능
        if (request.getStatus() == MemberStatus.DELETED) {
            throw new CustomException(ErrorCode.ADMIN_CANNOT_DELETE);
        }

        Member member = findMemberByStringId(memberId);

        if (!member.isAdmin()) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        Member targetMember = findMemberById(targetMemberId);

        // 현재 상태와 요청 상태가 같으면 예외 처리
        if (targetMember.getStatus() == request.getStatus()) {
            throw new CustomException(ErrorCode.MEMBER_STATUS_SAME);
        }

        targetMember.modifyStatus(request.getStatus());
    }

    // AuthenticationManager 를 통해 인증 처리
    private Authentication authenticateMember(LoginRequest dto) {
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword());
        return authenticationManager.authenticate(authToken);
    }

    // JWT access token, refresh token 생성
    private MemberTokenResponse generateMemberTokens(String role, String userId) {
        String access = jwtUtil.createJwt(ACCESS_TOKEN_CATEGORY, userId, role,
                jwtProperties.getAccessExpiration());
        String refresh = jwtUtil.createJwt(REFRESH_TOKEN_CATEGORY, userId, role,
                jwtProperties.getRefreshExpiration());

        saveRefreshTokenRedis(userId, refresh, jwtProperties.getRefreshExpiration());

        return MemberTokenResponse.of(access, refresh);
    }

    // Redis 에 refresh token 저장
    private void saveRefreshTokenRedis(String userId, String refreshToken, Long expirationMs) {
        redisRepository.saveRefreshToken(userId, refreshToken, expirationMs);
    }

    // 중복 회원 검증
    private void validateDuplicateMember(JoinRequest joinDto) {
        if (memberRepository.existsByUsername(joinDto.getUsername())) {
            throw new CustomException(ErrorCode.DUPLICATED_USERNAME);
        }
        if (memberRepository.existsByNickname(joinDto.getNickname())) {
            throw new CustomException(ErrorCode.DUPLICATED_NICKNAME);
        }
        if (memberRepository.existsByEmail(joinDto.getEmail())) {
            throw new CustomException(ErrorCode.DUPLICATED_EMAIL);
        }
    }

    // 인증 코드 생성
    private int generateAuthCode() {
        // 6자리 랜덤 인증 코드 생성
        return ThreadLocalRandom.current().nextInt(100000, 1000000);
    }

    // Redis 에 인증 코드 저장
    private void saveAuthCodeRedis(String email, String authCode) {
        redisRepository.saveEmailAuthCode(email, authCode);
    }

    // 인증 코드 검증
    private void verifyAuthCodeRedis(String email, int authCode) {
        String redisCode = redisRepository.getAuthCode(email);

        if (redisCode == null) {
            throw new CustomException(ErrorCode.AUTH_CODE_NOT_FOUND);
        }

        if (!redisCode.equals(String.valueOf(authCode))) {
            throw new CustomException(ErrorCode.INVALID_AUTH_CODE);
        }
    }

    public Member findMemberByStringId(String memberId) {
        Long parsedId = Long.valueOf(memberId);
        return memberRepository.findById(parsedId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }

    public Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }
}
