package com.dorandoran.domain.post.service;

import com.dorandoran.SpringBootTestSupporter;
import com.dorandoran.domain.category.entity.Category;
import com.dorandoran.domain.category.entity.CategoryGroup;
import com.dorandoran.domain.member.entity.Member;
import com.dorandoran.domain.post.dto.request.PostCreateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
class PostServiceTest extends SpringBootTestSupporter {

    private Member member;
    private List<CategoryGroup> categoryGroups;
    private Category category;

    @BeforeEach
    void setUp() {
        member = memberFactory.saveAndCreateMember(1).getFirst();
        categoryGroups = categoryGroupFactory.saveAndCreateDefaultCategoryGroup();
        category = categoryFactory.saveAndCreateCategory("게임", "lol");
    }

    @DisplayName("게시글 생성 실패 - 존재하지 않는 회원")
    @Test
    void createPost_Fail_NonExistentMember() {
        // given
        String memberId = "9999";
        String categoryName = "lol";
        PostCreateRequest request = new PostCreateRequest("게시글 제목", "게시글 내용");
        List<MultipartFile> files = List.of(
                new MockMultipartFile(
                        "files",
                        "image1.png",
                        "image/png",
                        "dummy image content".getBytes()
                ),
                new MockMultipartFile(
                        "files",
                        "image2.jpg",
                        "image/jpeg",
                        "dummy image content".getBytes()
                )
        );

        // when // then
        assertThatThrownBy(() -> postService.createPost(memberId, categoryName, request, files))
                .isInstanceOf(Exception.class)
                .extracting("code")
                .isEqualTo(com.dorandoran.global.response.ErrorCode.MEMBER_NOT_FOUND);
    }

    @DisplayName("게시글 생성 실패 - 존재하지 않는 카테고리")
    @Test
    void createPost_Fail_NonExistentCategory() {
        // given
        String memberId = member.getId().toString();
        String categoryName = "nonexistent-category";
        PostCreateRequest request = new PostCreateRequest("게시글 제목", "게시글 내용");
        List<MultipartFile> files = List.of(
                new MockMultipartFile(
                        "files",
                        "image1.png",
                        "image/png",
                        "dummy image content".getBytes()
                ),
                new MockMultipartFile(
                        "files",
                        "image2.jpg",
                        "image/jpeg",
                        "dummy image content".getBytes()
                )
        );

        // when // then
        assertThatThrownBy(() -> postService.createPost(memberId, categoryName, request, files))
                .isInstanceOf(Exception.class)
                .extracting("code")
                .isEqualTo(com.dorandoran.global.response.ErrorCode.CATEGORY_NOT_FOUND);
    }

    @DisplayName("미디어 저장 실패 - 잘못된 파일 형식")
    @Test
    void mediaType_Fail() {
        // given
        MultipartFile invalidFile = new MockMultipartFile(
                "files",
                "document.pdf",
                "application/pdf",
                "dummy pdf content".getBytes()
        );

        // when // then
        assertThatThrownBy(() -> {
            postService.createPost(
                    member.getId().toString(),
                    category.getAddress(),
                    new PostCreateRequest("게시글 제목", "게시글 내용"),
                    List.of(invalidFile)
            );
        })
                .isInstanceOf(Exception.class)
                .extracting("code")
                .isEqualTo(com.dorandoran.global.response.ErrorCode.INVALID_MEDIA_TYPE);
    }
}