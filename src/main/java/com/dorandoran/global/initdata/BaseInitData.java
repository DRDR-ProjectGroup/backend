package com.dorandoran.global.initdata;

import com.dorandoran.domain.category.entity.Category;
import com.dorandoran.domain.category.entity.CategoryGroup;
import com.dorandoran.domain.category.repository.CategoryGroupRepository;
import com.dorandoran.domain.category.repository.CategoryRepository;
import com.dorandoran.domain.member.entity.Member;
import com.dorandoran.domain.member.repository.MemberRepository;
import com.dorandoran.domain.member.service.MemberService;
import com.dorandoran.global.exception.CustomException;
import com.dorandoran.global.response.ErrorCode;
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
    private final CategoryRepository categoryRepository;
    private final CategoryGroupRepository categoryGroupRepository;
    private final PasswordEncoder passwordEncoder;
    private final MemberService memberService;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    void init() {
        memberService.createAdminMember();
        createMemberData(3);
        createDefaultCategoryGroup();
        createDefaultCategory();
    }

    private List<Member> createMemberData(int count) {
        if (memberRepository.count() > 1) {
            return memberRepository.findAll();
        }

        if (count == 0) {
            return null;
        }

        List<Member> memberList = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            String username = "test" + i;
            String email = "test" + i + "@email.com";
            String password = passwordEncoder.encode("test!1234");
            String nickname = "test" + i;

            memberList.add(memberRepository.save(Member.createMember(username, password, email, nickname)));
        }

        return memberList;
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

    private List<Category> createDefaultCategory() {
        if (categoryRepository.count() != 0) {
            return categoryRepository.findAll();
        }
        List<Category> categoryList = new ArrayList<>();
        categoryList.add(
                categoryRepository.save(
                        Category.createCategory(
                                categoryGroupRepository.findByName("일반").orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND)),
                                "자유",
                                "free"
                        )
                )
        );
        categoryList.add(
                categoryRepository.save(
                        Category.createCategory(
                                categoryGroupRepository.findByName("유머").orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND)),
                                "유머",
                                "humor"
                        )
                )
        );
        categoryList.add(
                categoryRepository.save(
                        Category.createCategory(
                                categoryGroupRepository.findByName("정보").orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND)),
                                "정보",
                                "info"
                        )
                )
        );
        categoryList.add(
                categoryRepository.save(
                        Category.createCategory(
                                categoryGroupRepository.findByName("게임").orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND)),
                                "롤",
                                "lol"
                        )
                )
        );
        return categoryList;
    }
}
