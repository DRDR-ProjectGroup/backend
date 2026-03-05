package com.dorandoran.domain.post.scheduler;

import com.dorandoran.domain.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostCleanupScheduler {

    private final PostService postService;

    @Scheduled(cron = "0 0 5 * * *") // 매일 오전 5시에 실행
    public void cleanUp() {
        postService.deleteExpiredPost();
    }

    @Scheduled(cron = "0 0 5 * * *") // 매일 오전 5시에 실행
    public void cleanUpCategoriesAndGroups() {
        postService.deleteCategoryAndGroup();
    }
}
