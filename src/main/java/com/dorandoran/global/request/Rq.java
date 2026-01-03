package com.dorandoran.global.request;

import com.dorandoran.domain.member.entity.Member;
import com.dorandoran.domain.member.repository.MemberRepository;
import com.dorandoran.global.response.ErrorCode;
import com.dorandoran.global.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.util.Optional;

import static com.dorandoran.global.response.ErrorCode.NEED_LOGIN;

@Component
@RequestScope
@RequiredArgsConstructor
public class Rq {
    private final MemberRepository memberRepository;
    private Member actor;

    public Member getActor() {
        if (actor == null) {
            actor = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                    .map(Authentication::getPrincipal)
                    .filter(principal -> principal instanceof SecurityUser)
                    .map(principal -> (SecurityUser) principal)
                    .flatMap(securityUser -> memberRepository.findByEmail(securityUser.getEmail()))
                    .orElseThrow(NEED_LOGIN::throwCustomException);
        }

        return actor;
    }

    public Member assertIsOwner(Long targetMemberId) {
        Member actor = getActor();

        if (!actor.getId().equals(targetMemberId)) {
            throw ErrorCode.FORBIDDEN_ACCESS.throwCustomException();
        }

        return actor;
    }

}