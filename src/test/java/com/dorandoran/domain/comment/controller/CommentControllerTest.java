package com.dorandoran.domain.comment.controller;

import com.dorandoran.SpringBootTestSupporter;
import com.dorandoran.domain.category.entity.Category;
import com.dorandoran.domain.category.entity.CategoryGroup;
import com.dorandoran.domain.comment.dto.request.CommentModifyRequest;
import com.dorandoran.domain.comment.dto.request.CommentRequest;
import com.dorandoran.domain.comment.entity.Comment;
import com.dorandoran.domain.member.entity.Member;
import com.dorandoran.domain.post.entity.Post;
import com.dorandoran.global.response.SuccessCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
class CommentControllerTest extends SpringBootTestSupporter {

    private static final String COMMENT_CONTENT = "댓글 내용입니다.";
    private List<Member> memberList;
    private Member member;
    private List<CategoryGroup> categoryGroups;
    private Category category;
    private List<Post> posts;
    private Post post;

    @BeforeEach
    void setUp() {
        memberList = memberFactory.saveAndCreateMember(10);
        member = memberList.getFirst();
        categoryGroups = categoryGroupFactory.saveAndCreateDefaultCategoryGroup();
        category = categoryFactory.saveAndCreateCategory("게임", "lol");
        posts = postFactory.saveAndCreatePost(member, category, 10);
        post = posts.getFirst();
    }

    @DisplayName("댓글 생성 테스트")
    @Test
    void createComment() throws Exception {
        // given
        CommentRequest request = new CommentRequest(null, COMMENT_CONTENT);

        // when
        ResultActions result = mockMvc.perform(post("/api/v1/posts/{postId}/comments", post.getId())
                .with(user(String.valueOf(member.getId())).roles("MEMBER"))
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request))
        );

        // then
        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value(SuccessCode.COMMENT_CREATE_SUCCESS.getMessage()));
    }

    @DisplayName("대댓글 생성 테스트")
    @Test
    void createReplyComment() throws Exception {
        // given
        CommentRequest parentRequest = new CommentRequest(null, "parent content");
        Long parentCommentId = commentFactory.saveAndCreateComment(post, member, null, parentRequest.getContent()).getId();

        CommentRequest replyRequest = new CommentRequest(parentCommentId, "child content");

        // when
        ResultActions result = mockMvc.perform(post("/api/v1/posts/{postId}/comments", post.getId())
                .with(user(String.valueOf(member.getId())).roles("MEMBER"))
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(replyRequest))
        );

        // then
        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value(SuccessCode.COMMENT_CREATE_SUCCESS.getMessage()));
    }

    @DisplayName("댓글 조회")
    @Test
    void getComments() throws Exception {
        // given
        List<Comment> comments = new ArrayList<>();
        for (int i = 1; i <= 30; i++) {
            comments.add(
                    commentFactory.saveAndCreateComment(
                            post,
                            member,
                            null,
                            "댓글 내용 " + i
                    )
            );
        }

        // when
        ResultActions result = mockMvc.perform(get("/api/v1/posts/{postId}/comments", post.getId()));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(SuccessCode.COMMENT_LIST_READ_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data.comments[0].content").value(comments.getFirst().getContent()));
    }

    @DisplayName("댓글 조회 - 댓글 없음")
    @Test
    void getNoComments() throws Exception {
        // given

        // when
        ResultActions result = mockMvc.perform(get("/api/v1/posts/{postId}/comments", post.getId()));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(SuccessCode.COMMENT_LIST_READ_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data.comments").value(new ArrayList<>()));
    }

    @DisplayName("댓글 수정")
    @Test
    void modifyComment() throws Exception {
        // given
        Comment comment = commentFactory.saveAndCreateComment(post, member, null, "before");
        String modifiedContent = "after";
        CommentModifyRequest request = new CommentModifyRequest(modifiedContent);

        // when
        ResultActions result = mockMvc.perform(patch("/api/v1/posts/{postId}/comments/{commentId}", post.getId(), comment.getId())
                .with(user(String.valueOf(member.getId())).roles("MEMBER"))
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request))
        );

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(SuccessCode.COMMENT_MODIFY_SUCCESS.getMessage()));
    }

    @DisplayName("댓글 삭제")
    @Test
    void deleteComment() throws Exception {
        // given
        Comment comment = commentFactory.saveAndCreateComment(post, member, null, "content");

        // when
        ResultActions result = mockMvc.perform(delete("/api/v1/posts/{postId}/comments/{commentId}", post.getId(), comment.getId())
                .with(user(String.valueOf(member.getId())).roles("MEMBER"))
        );

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(SuccessCode.COMMENT_DELETE_SUCCESS.getMessage()));
    }
}