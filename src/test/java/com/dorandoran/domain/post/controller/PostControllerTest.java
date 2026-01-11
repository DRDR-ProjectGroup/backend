package com.dorandoran.domain.post.controller;

import com.dorandoran.SpringBootTestSupporter;
import com.dorandoran.domain.category.entity.Category;
import com.dorandoran.domain.category.entity.CategoryGroup;
import com.dorandoran.domain.member.entity.Member;
import com.dorandoran.domain.post.dto.request.PostCreateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
class PostControllerTest extends SpringBootTestSupporter {

    private Member member;
    private List<CategoryGroup> categoryGroups;
    private Category category;

    @BeforeEach
    void setUp() {
        member = memberFactory.saveAndCreateMember(1).getFirst();
        categoryGroups = categoryGroupFactory.saveAndCreateDefaultCategoryGroup();
        category = categoryFactory.saveAndCreateCategory("게임", "lol");
    }

    @DisplayName("게시글 생성")
    @Test
    void createPost() throws Exception {
        // given
        String categoryName = "lol";
        PostCreateRequest request = new PostCreateRequest("게시글 제목", "게시글 내용");

        MockMultipartFile postPart = new MockMultipartFile(
                "post",
                "post.json",
                "application/json",
                objectMapper.writeValueAsBytes(request)
        );

        // when
        ResultActions result = mockMvc.perform(multipart("/api/v1/posts/{categoryName}", categoryName)
                .file(postPart)
                .with(user(String.valueOf(member.getId())).roles("MEMBER"))
        );

        // then
        result.andExpect(status().isCreated());
    }

    @DisplayName("게시글 생성 - 파일 업로드 포함")
    @Test
    void createPostWithFiles() throws Exception {
        // given
        String categoryName = "lol";
        PostCreateRequest request = new PostCreateRequest("게시글 제목", "게시글 내용");

        MockMultipartFile postPart = new MockMultipartFile(
                "post",
                "post.json",
                "application/json",
                objectMapper.writeValueAsBytes(request)
        );
        MockMultipartFile file1 = new MockMultipartFile(
                "files",
                "image1.png",
                "image/png",
                "dummy image content 1".getBytes()
        );
        MockMultipartFile file2 = new MockMultipartFile(
                "files",
                "image2.mp4",
                "video/mp4",
                "dummy image content 2".getBytes()
        );

        // when
        ResultActions result = mockMvc.perform(multipart("/api/v1/posts/{categoryName}", categoryName)
                .file(postPart)
                .file(file1)
                .file(file2)
                .with(user(String.valueOf(member.getId())).roles("MEMBER"))
        );

        // then
        result.andExpect(status().isCreated());
    }
}