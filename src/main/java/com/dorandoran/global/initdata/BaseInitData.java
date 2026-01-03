package com.dorandoran.global.initdata;

import com.dorandoran.domain.member.entity.Member;
import com.dorandoran.domain.member.repository.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Profile("dev")
@Component
@RequiredArgsConstructor
public class BaseInitData {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    void init() {
        List<Member> savedMemberData = createMemberData(3);
    }

    private List<Member> createMemberData(int count) {
        if (memberRepository.count() != 0) {
            return memberRepository.findAll();
        }

        if (count == 0) {
            return null;
        }

        List<Member> memberList = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            String username = "test" + i;
            String email = "test" + i + "@example.com";
            String password = passwordEncoder.encode("test1234");
            String nickname = "테스트" + i;

            memberList.add(memberRepository.save(Member.createMember(username, password, email, nickname)));
        }

        return memberList;
    }
}
