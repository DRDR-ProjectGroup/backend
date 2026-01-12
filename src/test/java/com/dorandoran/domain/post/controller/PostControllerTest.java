package com.dorandoran.domain.post.controller;

import com.dorandoran.SpringBootTestSupporter;
import com.dorandoran.domain.category.entity.Category;
import com.dorandoran.domain.category.entity.CategoryGroup;
import com.dorandoran.domain.member.entity.Member;
import com.dorandoran.domain.post.dto.request.PostCreateRequest;
import com.dorandoran.domain.post.dto.response.PostMediaResponse;
import com.dorandoran.domain.post.dto.response.PostResponse;
import com.dorandoran.domain.post.entity.Post;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.dorandoran.global.response.SuccessCode.POST_DETAIL_SUCCESS;
import static com.dorandoran.global.response.SuccessCode.POST_MODIFY_SUCCESS;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
class PostControllerTest extends SpringBootTestSupporter {

    private Member member;
    private List<CategoryGroup> categoryGroups;
    private Category category;
    private Post post;

    @BeforeEach
    void setUp() {
        member = memberFactory.saveAndCreateMember(1).getFirst();
        categoryGroups = categoryGroupFactory.saveAndCreateDefaultCategoryGroup();
        category = categoryFactory.saveAndCreateCategory("게임", "lol");
        post = postFactory.saveAndCreatePost(member, category, 1).getFirst();
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

    @DisplayName("게시글 조회")
    @Test
    void getPost() throws Exception {
        // given
        Long postId = post.getId();
        List<PostMediaResponse> mediaResponses = post.getPostMediaList().stream()
                .map(PostMediaResponse::of)
                .toList();
        PostResponse expectedResponse = PostResponse.of(post, mediaResponses);

        // when
        ResultActions result = mockMvc.perform(get("/api/v1/posts/{postId}", postId)
                .with(user(String.valueOf(member.getId())).roles("MEMBER"))
        );

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(POST_DETAIL_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.code").value(POST_DETAIL_SUCCESS.getHttpStatus().value()))
                .andExpect(jsonPath("$.data.postId").value(expectedResponse.getPostId()))
                .andExpect(jsonPath("$.data.title").value(expectedResponse.getTitle()))
                .andExpect(jsonPath("$.data.content").value(expectedResponse.getContent()))
                .andExpect(jsonPath("$.data.mediaList[0].url").value(expectedResponse.getMediaList().getFirst().getUrl()))
                .andExpect(jsonPath("$.data.mediaList[0].order").value(expectedResponse.getMediaList().getFirst().getOrder()))
        ;
    }

    @DisplayName("게시글 수정")
    @Test
    void modifyPost() throws Exception {
        // given
        Long postId = post.getId();
        PostCreateRequest request = new PostCreateRequest("수정된 제목", "수정된 내용");

        MockMultipartFile postPart = new MockMultipartFile(
                "post",
                "post.json",
                "application/json",
                objectMapper.writeValueAsBytes(request)
        );
        MockMultipartFile file1 = new MockMultipartFile(
                "files",
                "updated_image1.png",
                "image/png",
                "updated dummy image content 1".getBytes()
        );

        // when
        ResultActions result = mockMvc.perform(multipart("/api/v1/posts/{postId}", postId)
                .file(postPart)
                .file(file1)
                .with(user(String.valueOf(member.getId())).roles("MEMBER"))
                .with(requestPostProcessor -> {
                    requestPostProcessor.setMethod("PUT");
                    return requestPostProcessor;
                })
        );

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(POST_MODIFY_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.code").value(POST_MODIFY_SUCCESS.getHttpStatus().value()))
                .andExpect(jsonPath("$.data.title").value(request.getTitle()))
                .andExpect(jsonPath("$.data.content").value(request.getContent()))
                .andExpect(jsonPath("$.data.mediaList[0].url").exists())
                .andExpect(jsonPath("$.data.mediaList[0].order").value(0))
        ;
    }

    @DisplayName("게시글 삭제")
    @Test
    void deletePost() throws Exception {
        // given
        Long postId = post.getId();

        // when
        ResultActions result = mockMvc.perform(delete("/api/v1/posts/{postId}", postId)
                .with(user(String.valueOf(member.getId())).roles("MEMBER"))
        );

        // then
        result.andExpect(status().isOk());
    }
}