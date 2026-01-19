package com.dorandoran.domain.admin.controller;

import com.dorandoran.SpringBootTestSupporter;
import com.dorandoran.domain.category.dto.request.CategoryGroupRequest;
import com.dorandoran.domain.category.dto.request.CategoryRequest;
import com.dorandoran.domain.category.entity.Category;
import com.dorandoran.domain.category.entity.CategoryGroup;
import com.dorandoran.domain.member.dto.request.MemberStatusRequest;
import com.dorandoran.domain.member.entity.Member;
import com.dorandoran.domain.member.type.MemberStatus;
import com.dorandoran.domain.post.entity.Post;
import com.dorandoran.global.response.ErrorCode;
import com.dorandoran.global.response.SuccessCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
class AdminControllerTest extends SpringBootTestSupporter {

    private Member admin;
    private List<CategoryGroup> categoryGroups;
    private Category category;
    private Category otherCategory;
    private List<Member> memberList;
    private List<Post> posts;

    @BeforeEach
    void setUp() {
        admin = memberFactory.saveAndCreateAdminMember();
        categoryGroups = categoryGroupFactory.saveAndCreateDefaultCategoryGroup();
        category = categoryFactory.saveAndCreateCategory("게임", "lol");
        otherCategory = categoryFactory.saveAndCreateCategory("유머", "free");
        memberList = memberFactory.saveAndCreateMember(10);
        posts = postFactory.saveAndCreatePost(memberList.getFirst(), category, 10);
    }

    @DisplayName("그룹 생성")
    @Test
    void createCategoryGroup() throws Exception {
        // given
        CategoryGroupRequest request = new CategoryGroupRequest("새로운 그룹");

        // when
        ResultActions result = mockMvc.perform(post("/api/v1/admin/groups")
                .with(user(String.valueOf(admin.getId())).roles("ADMIN"))
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request))
        );

        // then
        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value(SuccessCode.CATEGORY_GROUP_CREATE_SUCCESS.getMessage()))
        ;
    }

    @DisplayName("그룹 생성 - 일반 회원 접근 불가")
    @Test
    void createCategoryGroup_AccessDenied() throws Exception {
        // given
        Member member = memberList.getFirst();
        CategoryGroupRequest request = new CategoryGroupRequest("새로운 그룹");

        // when
        ResultActions result = mockMvc.perform(post("/api/v1/admin/groups")
                .with(user(String.valueOf(member.getId())).roles("MEMBER"))
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request))
        );

        // then
        result.andExpect(status().isForbidden());
    }

    @DisplayName("그룹 생상 - 중복 이름으로 인한 실패")
    @Test
    void createCategoryGroup_Fail_DuplicateName() throws Exception {
        // given
        CategoryGroup existingGroup = categoryGroups.getFirst();
        CategoryGroupRequest request = new CategoryGroupRequest(existingGroup.getName());

        // when
        ResultActions result = mockMvc.perform(post("/api/v1/admin/groups")
                .with(user(String.valueOf(admin.getId())).roles("ADMIN"))
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request))
        );

        // then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.CATEGORY_GROUP_DUPLICATE_NAME.getMessage()));
    }

    @DisplayName("그룹 수정")
    @Test
    void modifyCategoryGroup() throws Exception {
        // given
        CategoryGroup categoryGroup = categoryGroups.getFirst();
        CategoryGroupRequest request = new CategoryGroupRequest("수정된 그룹명");

        // when
        ResultActions result = mockMvc.perform(put("/api/v1/admin/groups/" + categoryGroup.getId())
                .with(user(String.valueOf(admin.getId())).roles("ADMIN"))
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request))
        );

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(SuccessCode.CATEGORY_GROUP_MODIFY_SUCCESS.getMessage()))
        ;
    }

    @DisplayName("그룹 수정 - 일반 회원 접근 불가")
    @Test
    void modifyCategoryGroup_AccessDenied() throws Exception {
        // given
        Member member = memberList.getFirst();
        CategoryGroup categoryGroup = categoryGroups.getFirst();
        CategoryGroupRequest request = new CategoryGroupRequest("수정된 그룹명");

        // when
        ResultActions result = mockMvc.perform(put("/api/v1/admin/groups/" + categoryGroup.getId())
                .with(user(String.valueOf(member.getId())).roles("MEMBER"))
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request))
        );

        // then
        result.andExpect(status().isForbidden());
    }

    @DisplayName("그룹 수정 - 중복 이름으로 인한 실패")
    @Test
    void modifyCategoryGroup_Fail_DuplicateName() throws Exception {
        // given
        CategoryGroup categoryGroup = categoryGroups.getFirst();
        CategoryGroup anotherGroup = categoryGroups.getLast();
        CategoryGroupRequest request = new CategoryGroupRequest(anotherGroup.getName());

        // when
        ResultActions result = mockMvc.perform(put("/api/v1/admin/groups/" + categoryGroup.getId())
                .with(user(String.valueOf(admin.getId())).roles("ADMIN"))
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request))
        );

        // then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.CATEGORY_GROUP_DUPLICATE_NAME.getMessage()));
    }

    @DisplayName("그룹 삭제")
    @Test
    void deleteCategoryGroup() throws Exception {
        // given
        CategoryGroup categoryGroup = categoryGroups.getFirst();

        // when
        ResultActions result = mockMvc.perform(delete("/api/v1/admin/groups/" + categoryGroup.getId())
                .with(user(String.valueOf(admin.getId())).roles("ADMIN"))
        );

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(SuccessCode.CATEGORY_GROUP_DELETE_SUCCESS.getMessage()))
        ;
    }

    @DisplayName("그룹 삭제 - 일반 회원 접근 불가")
    @Test
    void deleteCategoryGroup_AccessDenied() throws Exception {
        // given
        Member member = memberList.getFirst();
        CategoryGroup categoryGroup = categoryGroups.getFirst();

        // when
        ResultActions result = mockMvc.perform(delete("/api/v1/admin/groups/" + categoryGroup.getId())
                .with(user(String.valueOf(member.getId())).roles("MEMBER"))
        );

        // then
        result.andExpect(status().isForbidden());
    }

    @DisplayName("그룹 삭제 - 카테고리 존재로 인한 실패")
    @Test
    void deleteCategoryGroup_Fail_HasCategories() throws Exception {
        // given
        CategoryGroup categoryGroup = categoryGroups.getLast();

        // when
        ResultActions result = mockMvc.perform(delete("/api/v1/admin/groups/" + categoryGroup.getId())
                .with(user(String.valueOf(admin.getId())).roles("ADMIN"))
        );

        // then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.CATEGORY_GROUP_DELETE_FAIL_HAS_CATEGORIES.getMessage()));
    }

    @DisplayName("카테고리 생성")
    @Test
    void createCategory() throws Exception {
        // given
        CategoryRequest request = new CategoryRequest(categoryGroups.getFirst().getId(), "새로운 카테고리", "newCategory");

        // when
        ResultActions result = mockMvc.perform(post("/api/v1/admin/categories")
                .with(user(String.valueOf(admin.getId())).roles("ADMIN"))
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request))
        );

        // then
        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value(SuccessCode.CATEGORY_CREATE_SUCCESS.getMessage()));
    }

    @DisplayName("카테고리 생성 - 일반 회원 접근 불가")
    @Test
    void createCategory_AccessDenied() throws Exception {
        // given
        Member member = memberList.getFirst();
        CategoryRequest request = new CategoryRequest(categoryGroups.getFirst().getId(), "새로운 카테고리", "newCategory");

        // when
        ResultActions result = mockMvc.perform(post("/api/v1/admin/categories")
                .with(user(String.valueOf(member.getId())).roles("MEMBER"))
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request))
        );

        // then
        result.andExpect(status().isForbidden());
    }

    @DisplayName("카테고리 생성 - 중복 이름/주소로 인한 실패")
    @Test
    void createCategory_Fail_DuplicateNameOrAddress() throws Exception {
        // given
        CategoryRequest request = new CategoryRequest(categoryGroups.getFirst().getId(), category.getName(), category.getAddress());

        // when
        ResultActions result = mockMvc.perform(post("/api/v1/admin/categories")
                .with(user(String.valueOf(admin.getId())).roles("ADMIN"))
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request))
        );

        // then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.CATEGORY_DUPLICATE.getMessage()));
    }

    @DisplayName("카테고리 수정")
    @Test
    void modifyCategory() throws Exception {
        // given
        CategoryRequest request = new CategoryRequest(categoryGroups.getLast().getId(), "수정된 카테고리", "modifiedCategory");

        // when
        ResultActions result = mockMvc.perform(put("/api/v1/admin/categories/" + category.getId())
                .with(user(String.valueOf(admin.getId())).roles("ADMIN"))
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request))
        );

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(SuccessCode.CATEGORY_MODIFY_SUCCESS.getMessage()));
    }

    @DisplayName("카테고리 수정 - 일반 회원 접근 불가")
    @Test
    void modifyCategory_AccessDenied() throws Exception {
        // given
        Member member = memberList.getFirst();
        CategoryRequest request = new CategoryRequest(categoryGroups.getLast().getId(), "수정된 카테고리", "modifiedCategory");

        // when
        ResultActions result = mockMvc.perform(put("/api/v1/admin/categories/" + category.getId())
                .with(user(String.valueOf(member.getId())).roles("MEMBER"))
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request))
        );

        // then
        result.andExpect(status().isForbidden());
    }

    @DisplayName("카테고리 수정 - 중복 이름/주소로 인한 실패")
    @Test
    void modifyCategory_Fail_DuplicateNameOrAddress() throws Exception {
        // given
        CategoryRequest request = new CategoryRequest(categoryGroups.getFirst().getId(), category.getName(), category.getAddress());

        // when
        ResultActions result = mockMvc.perform(put("/api/v1/admin/categories/" + category.getId())
                .with(user(String.valueOf(admin.getId())).roles("ADMIN"))
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request))
        );

        // then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.CATEGORY_DUPLICATE.getMessage()));
    }

    @DisplayName("카테고리 삭제")
    @Test
    void deleteCategory() throws Exception {
        // given

        // when
        ResultActions result = mockMvc.perform(delete("/api/v1/admin/categories/" + otherCategory.getId())
                .with(user(String.valueOf(admin.getId())).roles("ADMIN"))
        );

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(SuccessCode.CATEGORY_DELETE_SUCCESS.getMessage()));
    }

    @DisplayName("카테고리 삭제 - 일반 회원 접근 불가")
    @Test
    void deleteCategory_AccessDenied() throws Exception {
        // given
        Member member = memberList.getFirst();

        // when
        ResultActions result = mockMvc.perform(delete("/api/v1/admin/categories/" + category.getId())
                .with(user(String.valueOf(member.getId())).roles("MEMBER"))
        );

        // then
        result.andExpect(status().isForbidden());
    }

    @DisplayName("카테고리 삭제 - 게시글 존재로 인한 실패")
    @Test
    void deleteCategory_Fail_HasPosts() throws Exception {
        // given

        // when
        ResultActions result = mockMvc.perform(delete("/api/v1/admin/categories/" + category.getId())
                .with(user(String.valueOf(admin.getId())).roles("ADMIN"))
        );

        // then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.CATEGORY_DELETE_FAIL_HAS_POSTS.getMessage()));
    }

    @DisplayName("회원 목록 조회")
    @Test
    void getMemberList() throws Exception {
        // given

        // when
        ResultActions result = mockMvc.perform(get("/api/v1/admin/members")
                .with(user(String.valueOf(admin.getId())).roles("ADMIN"))
                .param("page", "1")
                .param("size", "20")
        );

        // then
        result.andExpect(status().isOk());
        for (int i = 1; i <= 10; i++) {
            result.andExpect(jsonPath("$.data[" + i + "].memberId").value(memberList.get(i - 1).getId()))
                    .andExpect(jsonPath("$.data[" + i + "].username").value(memberList.get(i - 1).getUsername()))
                    .andExpect(jsonPath("$.data[" + i + "].nickname").value(memberList.get(i - 1).getNickname()))
                    .andExpect(jsonPath("$.data[" + i + "].email").value(memberList.get(i - 1).getEmail()))
            ;
        }
    }

    @DisplayName("회원 목록 조회 - 일반 회원 접근 불가")
    @Test
    void getMemberList_AccessDenied() throws Exception {
        // given
        Member member = memberList.getFirst();

        // when
        ResultActions result = mockMvc.perform(get("/api/v1/admin/members")
                .with(user(String.valueOf(member.getId())).roles("MEMBER"))
                .param("page", "0")
                .param("size", "10")
        );

        // then
        result.andExpect(status().isForbidden());
    }

    @DisplayName("회원 상태 수정")
    @Test
    void modifyMemberStatus() throws Exception {
        // given
        Member targetMember = memberList.getFirst();
        MemberStatusRequest request = new MemberStatusRequest(MemberStatus.BLOCKED);

        // when
        ResultActions result = mockMvc.perform(patch("/api/v1/admin/members/" + targetMember.getId())
                .with(user(String.valueOf(admin.getId())).roles("ADMIN"))
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request))
        );

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(SuccessCode.MEMBER_STATUS_CHANGE_SUCCESS.getMessage()));
    }

    @DisplayName("회원 상태 수정 - 일반 회원 접근 불가")
    @Test
    void modifyMemberStatus_AccessDenied() throws Exception {
        // given
        Member member = memberList.getFirst();
        Member targetMember = memberList.getLast();
        MemberStatusRequest request = new MemberStatusRequest(MemberStatus.BLOCKED);

        // when
        ResultActions result = mockMvc.perform(patch("/api/v1/admin/members/" + targetMember.getId())
                .with(user(String.valueOf(member.getId())).roles("MEMBER"))
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request))
        );

        // then
        result.andExpect(status().isForbidden());
    }

    @DisplayName("회원 상태 수정 - 탈퇴 처리 불가능")
    @Test
    void modifyMemberStatus_Fail_CannotResign() throws Exception {
        // given
        Member targetMember = memberList.getFirst();
        MemberStatusRequest request = new MemberStatusRequest(MemberStatus.DELETED);

        // when
        ResultActions result = mockMvc.perform(patch("/api/v1/admin/members/" + targetMember.getId())
                .with(user(String.valueOf(admin.getId())).roles("ADMIN"))
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request))
        );

        // then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.ADMIN_CANNOT_DELETE.getMessage()));
    }
}