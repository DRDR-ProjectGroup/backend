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
import com.dorandoran.standard.page.dto.PageMemberDto;
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
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

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
    public void logout(String memberId, String accessToken) {
        redisRepository.deleteRefreshToken(memberId);
        long expiration = jwtUtil.getExpiration(accessToken);
        redisRepository.addBlacklist(accessToken, expiration);
    }

    @Transactional
    public void resign(String memberId) {
        Member findMember = findMemberByStringId(memberId);

        redisRepository.deleteRefreshToken(memberId);
        memberRepository.delete(findMember);
    }

    @Transactional(readOnly = true)
    public MemberInfoResponse getMemberInfo(String memberId) {
        Member findMember = findMemberByStringId(memberId);

        return MemberInfoResponse.of(findMember);
    }

    @Transactional
    public void modifyNickname(String memberId, NicknameRequest nicknameDto) {
        Member findMember = findMemberByStringId(memberId);

        // 닉네임 중복 검사
        if (memberRepository.existsByNickname(nicknameDto.getNewNickname())) {
            throw new CustomException(ErrorCode.DUPLICATED_NICKNAME);
        }

        findMember.modifyNickname(nicknameDto.getNewNickname());
    }

    @Transactional
    public void modifyPassword(String memberId, PasswordRequest passwordDto) {
        Member findMember = findMemberByStringId(memberId);

        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(passwordDto.getPassword(), findMember.getPassword())) {
            throw new CustomException(ErrorCode.PASSWORD_MISMATCH);
        }

        // 새 비밀번호와 새 비밀번호 확인이 일치하는지 확인
        if (!passwordDto.getNewPassword().equals(passwordDto.getNewPassword2())) {
            throw new CustomException(ErrorCode.PASSWORDS_DO_NOT_MATCH);
        }

        // 새 비밀번호와 현재 비밀번호 일치 여부 확인
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

    @Value("${custom.tester.username}")
    private String testerUsername;

    @Value("${custom.tester.password}")
    private String testerPassword;

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

    @Transactional
    public void createTestMember() {
        if (memberRepository.findByUsername(testerUsername).isPresent()) {
            return;
        }

        String username = testerUsername;
        String password = passwordEncoder.encode(testerPassword);
        String email = testerUsername + "@naver.com";
        String nickname = "테스터";

        Member member = Member.createMember(username, password, email, nickname);

        memberRepository.save(member);
    }

    @Transactional(readOnly = true)
    public PageDto<PostListResponse> getMyPosts(String memberId, int page, int size) {
        Member findMember = findMemberByStringId(memberId);

        Pageable pageable = PageRequest.of(Math.max(0, page - 1), size, Sort.by(Sort.Order.desc("createdAt")));

        Page<PostListResponse> postListResponses = postRepository.findAllByMember(findMember, pageable).map(PostListResponse::of);

        return new PageDto<>(postListResponses, null);
    }

    @Transactional(readOnly = true)
    public PageCommentDto<CommentListMemberResponse> getMyComments(String memberId, int page, int size) {
        Member findMember = findMemberByStringId(memberId);

        Pageable pageable = PageRequest.of(Math.max(0, page - 1), size, Sort.by(Sort.Order.desc("createdAt")));

        Page<CommentListMemberResponse> commentPage = commentRepository.findAllByMember(findMember, pageable).map(CommentListMemberResponse::of);

        return new PageCommentDto<>(commentPage);
    }

    @Transactional
    public PageMemberDto<MemberDetailResponse> getAllMembers(String memberId, int page, int size) {
        Member member = findMemberByStringId(memberId);

        if (!member.isAdmin()) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        Pageable pageable = PageRequest.of(Math.max(0, page - 1), size, Sort.by(Sort.Order.asc("id")));

        Page<MemberDetailResponse> memberPage = memberRepository.findAll(pageable)
                .map(MemberDetailResponse::of);

        return new PageMemberDto<>(memberPage);
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

        Member targetMember = memberRepository.findById(targetMemberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // 타겟 멤버가 관리자일 경우 예외 처리
        if (targetMember.isAdmin()) {
            throw new CustomException(ErrorCode.ADMIN_NOT_CHAENGEABLE);
        }

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
    private MemberTokenResponse generateMemberTokens(String role, String memberId) {
        String access = jwtUtil.createJwt(ACCESS_TOKEN_CATEGORY, memberId, role,
                jwtProperties.getAccessExpiration());
        String refresh = jwtUtil.createJwt(REFRESH_TOKEN_CATEGORY, memberId, role,
                jwtProperties.getRefreshExpiration());

        saveRefreshTokenRedis(memberId, refresh, jwtProperties.getRefreshExpiration());

        return MemberTokenResponse.of(access, refresh);
    }

    // Redis 에 refresh token 저장
    private void saveRefreshTokenRedis(String memberId, String refreshToken, Long expirationMs) {
        redisRepository.saveRefreshToken(memberId, refreshToken, expirationMs);
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

        return findMemberById(parsedId);
    }

    public Member findMemberById(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        if (member.getStatus() == MemberStatus.DELETED) {
            throw new CustomException(ErrorCode.MEMBER_DELETED);
        } else if (member.getStatus() == MemberStatus.BLOCKED) {
            throw new CustomException(ErrorCode.MEMBER_BLOCKED);
        }

        return member;
    }

    public List<Member> findAllByIds(Set<String> members) {
        Set<Long> memberIds = members.stream().map(Long::valueOf).collect(Collectors.toSet());

        return memberRepository.findAllById(memberIds);
    }
}
