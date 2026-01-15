package com.dorandoran.global.initdata;

import com.dorandoran.domain.category.entity.CategoryGroup;
import com.dorandoran.domain.category.repository.CategoryGroupRepository;
import com.dorandoran.domain.member.service.MemberService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Profile("prod")
@Component
@RequiredArgsConstructor
public class BaseProdInitData {

    private final CategoryGroupRepository categoryGroupRepository;
    private final MemberService memberService;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    void init() {
        createDefaultCategoryGroup();
        memberService.createAdminMember();
    }

    private List<CategoryGroup> createDefaultCategoryGroup() {
        if (categoryGroupRepository.count() != 0) {
            return categoryGroupRepository.findAll();
        }
        List<CategoryGroup> categoryGroupList = new ArrayList<>();
        categoryGroupList.add(categoryGroupRepository.save(CategoryGroup.createCategoryGroup("일반")));
        categoryGroupList.add(categoryGroupRepository.save(CategoryGroup.createCategoryGroup("유머")));
        categoryGroupList.add(categoryGroupRepository.save(CategoryGroup.createCategoryGroup("정보")));
        categoryGroupList.add(categoryGroupRepository.save(CategoryGroup.createCategoryGroup("게임")));
        return categoryGroupList;
    }
}
