package com.dorandoran.global.initdata;

import com.dorandoran.domain.category.entity.Category;
import com.dorandoran.domain.category.entity.CategoryGroup;
import com.dorandoran.domain.category.repository.CategoryGroupRepository;
import com.dorandoran.domain.category.repository.CategoryRepository;
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
    private final CategoryRepository categoryRepository;
    private final CategoryGroupRepository categoryGroupRepository;
    private final PasswordEncoder passwordEncoder;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    void init() {
        List<Member> savedMemberData = createMemberData(3);
        List<CategoryGroup> defaultCategoryGroup = createDefaultCategoryGroup();
        List<Category> defaultCategory = createDefaultCategory();
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
            String email = "test" + i + "@email.com";
            String password = passwordEncoder.encode("test1234");
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
                                categoryGroupRepository.findByName("일반").orElse(null),
                                "자유",
                                "free"
                        )
                )
        );
        categoryList.add(
                categoryRepository.save(
                        Category.createCategory(
                                categoryGroupRepository.findByName("유머").orElse(null),
                                "유머",
                                "humor"
                        )
                )
        );
        categoryList.add(
                categoryRepository.save(
                        Category.createCategory(
                                categoryGroupRepository.findByName("게임").orElse(null),
                                "롤",
                                "lol"
                        )
                )
        );
        return categoryList;
    }
}
