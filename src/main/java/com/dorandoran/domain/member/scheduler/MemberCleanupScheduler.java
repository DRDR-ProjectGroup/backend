package com.dorandoran.domain.member.scheduler;

import com.dorandoran.domain.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberCleanupScheduler {

    private final MemberService memberService;

    @Scheduled(cron = "0 0 5 * * *") // 매일 오전 5시에 실행
    public void cleanUp() {
        memberService.deleteExpiredMember();
    }
}
