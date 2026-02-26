package com.dorandoran.domain.comment.service;

import com.dorandoran.SpringBootTestSupporter;
import com.dorandoran.domain.category.entity.Category;
import com.dorandoran.domain.category.entity.CategoryGroup;
import com.dorandoran.domain.comment.dto.request.CommentModifyRequest;
import com.dorandoran.domain.comment.entity.Comment;
import com.dorandoran.domain.member.entity.Member;
import com.dorandoran.domain.post.entity.Post;
import com.dorandoran.global.response.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
class CommentServiceTest extends SpringBootTestSupporter {

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

    @DisplayName("댓글 수정(삭제) - 작성자 불일치")
    @Test
    void modifyComment_AuthorMismatch() {
        // given
        Member otherMember = memberList.get(1);
        Comment comment = commentFactory.saveAndCreateComment(post, member, null, "original content");
        CommentModifyRequest request = new CommentModifyRequest("modify content");

        // when & then
        assertThatThrownBy(() -> commentService.modifyComment(post.getId(), comment.getId(), request, otherMember.getId().toString()))
                .isInstanceOf(Exception.class)
                .extracting("code")
                .isEqualTo(ErrorCode.FORBIDDEN);
    }
}