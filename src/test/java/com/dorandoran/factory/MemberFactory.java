package com.dorandoran.factory;

import com.dorandoran.domain.member.entity.Member;
import com.dorandoran.domain.member.repository.MemberRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public final class MemberFactory {

    public static final String MEMBER_PREFIX = "test";
    public static final String NAVER_EMAIL = "@naver.com";
    public static final String MEMBER_PW = "test@1234";

    private final EntityManager em;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * count :  생성할 회원 수량. 1부터 시작
     * 0일 경우, 생성하지 않음.
     */
    public List<Member> saveAndCreateMember(final int count) {
        if (count == 0) return List.of();

        ArrayList<Member> savedUserList = new ArrayList<>(count);

        for (int index = 1; index <= count; index++) {
            String username = String.format("%s%d", MEMBER_PREFIX, index);
            String password = passwordEncoder.encode(MEMBER_PW);
            String email = String.format("%s%d%s", MEMBER_PREFIX, index, NAVER_EMAIL);
            String nickname = String.format("%s%d", MEMBER_PREFIX, index);

            Member newMember = Member.createMember(username, password, email, nickname);
            Member saveMember = memberRepository.save(newMember);

            savedUserList.add(saveMember);
        }

        flushAndClear();

        return savedUserList;
    }

    private void flushAndClear() {
        em.flush();
        em.clear();
    }
}
