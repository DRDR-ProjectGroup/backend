package com.dorandoran.global.initdata;

import com.dorandoran.domain.category.entity.Category;
import com.dorandoran.domain.category.entity.CategoryGroup;
import com.dorandoran.domain.category.repository.CategoryGroupRepository;
import com.dorandoran.domain.category.repository.CategoryRepository;
import com.dorandoran.domain.member.entity.Member;
import com.dorandoran.domain.member.repository.MemberRepository;
import com.dorandoran.domain.post.entity.Post;
import com.dorandoran.domain.post.entity.PostMedia;
import com.dorandoran.domain.post.repository.PostMediaRepository;
import com.dorandoran.domain.post.repository.PostRepository;
import com.dorandoran.domain.post.type.MediaType;
import com.dorandoran.global.exception.CustomException;
import com.dorandoran.global.response.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
    private final PostRepository postRepository;
    private final PostMediaRepository postMediaRepository;

    @Value("${custom.admin.username}")
    private String adminUsername;

    @Value("${custom.admin.password}")
    private String adminPassword;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    void init() {
        List<Member> savedMemberData = createMemberData(3);
        List<CategoryGroup> defaultCategoryGroup = createDefaultCategoryGroup();
        List<Category> defaultCategory = createDefaultCategory();
        createPostAndPostMediaData();
        createAdminMember();
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

    private void createAdminMember() {
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
                                categoryGroupRepository.findByName("게임").orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND)),
                                "롤",
                                "lol"
                        )
                )
        );
        return categoryList;
    }

    private void createPostAndPostMediaData() {
        if (postRepository.count() != 0) {
            return;
        }

        for (int i = 1; i <= 5; i++) {
            Post post = Post.createPost(
                    memberRepository.findById(1L).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND)),
                    categoryRepository.findByAddress("free").orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND)),
                    "게시글 제목 " + i,
                    "게시글 내용 " + i
            );

            PostMedia postMedia = PostMedia.createPostMedia(
                    post,
                    MediaType.IMAGE,
                    "temp%d.jpg".formatted(i),
                    "stored_temp%d.jpg".formatted(i),
                    "http://example.com/stored_temp%d.jpg".formatted(i),
                    2048L,
                    0
            );

            post.addMedia(postMedia);

            postRepository.save(post);
        }

        for (int i = 1; i <= 5; i++) {
            Post post = Post.createPost(
                    memberRepository.findById(1L).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND)),
                    categoryRepository.findByAddress("lol").orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND)),
                    "게시글 제목 " + i,
                    "게시글 내용 " + i
            );

            PostMedia postMedia = PostMedia.createPostMedia(
                    post,
                    MediaType.IMAGE,
                    "temp%d.jpg".formatted(i),
                    "stored_temp%d.jpg".formatted(i),
                    "http://example.com/stored_temp%d.jpg".formatted(i),
                    2048L,
                    0
            );

            post.addMedia(postMedia);

            postRepository.save(post);
        }
    }
}
