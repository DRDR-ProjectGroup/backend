package com.dorandoran.domain.admin.controller;

import com.dorandoran.domain.category.dto.request.CategoryGroupRequest;
import com.dorandoran.domain.category.dto.request.CategoryRequest;
import com.dorandoran.domain.category.service.CategoryService;
import com.dorandoran.domain.member.dto.request.MemberStatusRequest;
import com.dorandoran.domain.member.dto.response.MemberDetailResponse;
import com.dorandoran.domain.member.service.MemberService;
import com.dorandoran.global.response.BaseResponse;
import com.dorandoran.global.response.SuccessCode;
import com.dorandoran.standard.page.dto.PageMemberDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "AdminController", description = "관리자 관련 API")
@Slf4j
public class AdminController {

    private final CategoryService categoryService;
    private final MemberService memberService;

    @PostMapping("/groups")
    @Operation(summary = "카테고리 그룹 생성", description = "새로운 카테고리 그룹을 생성합니다.")
    @SecurityRequirement(name = "bearerAuth")
    public BaseResponse<Void> createCategoryGroup(
            @RequestBody CategoryGroupRequest request,
            Principal principal
    ) {
        log.info("Admin '{}' is creating a new category group with name '{}'", principal.getName(), request.getGroupName());
        categoryService.createCategoryGroup(request, principal.getName());
        return BaseResponse.ok(SuccessCode.CATEGORY_GROUP_CREATE_SUCCESS);
    }

    @PutMapping("/groups/{groupId}")
    @Operation(summary = "카테고리 그룹명 수정", description = "기존 카테고리 그룹명을 수정합니다.")
    @SecurityRequirement(name = "bearerAuth")
    public BaseResponse<Void> updateCategoryGroup(
            @PathVariable Long groupId,
            @RequestBody CategoryGroupRequest request,
            Principal principal
    ) {
        log.info("Admin '{}' is modifying category group ID '{}' to new name '{}'", principal.getName(), groupId, request.getGroupName());
        categoryService.modifyCategoryGroup(groupId, request, principal.getName());
        return BaseResponse.ok(SuccessCode.CATEGORY_GROUP_MODIFY_SUCCESS);
    }

    @DeleteMapping("/groups/{groupId}")
    @Operation(summary = "카테고리 그룹 삭제", description = "기존 카테고리 그룹을 삭제합니다.")
    @SecurityRequirement(name = "bearerAuth")
    public BaseResponse<Void> deleteCategoryGroup(
            @PathVariable Long groupId,
            Principal principal
    ) {
        log.info("Admin '{}' is deleting category group ID '{}'", principal.getName(), groupId);
        categoryService.deleteCategoryGroup(groupId, principal.getName());
        return BaseResponse.ok(SuccessCode.CATEGORY_GROUP_DELETE_SUCCESS);
    }

    @PostMapping("/categories")
    @Operation(summary = "카테고리 생성", description = "새로운 카테고리를 생성합니다.")
    @SecurityRequirement(name = "bearerAuth")
    public BaseResponse<Void> createCategory(
            @RequestBody CategoryRequest request,
            Principal principal
    ) {
        log.info("Admin '{}' is creating a new category with name '{}'", principal.getName(), request.getCategoryName());
        categoryService.createCategory(request, principal.getName());
        return BaseResponse.ok(SuccessCode.CATEGORY_CREATE_SUCCESS);
    }

    @PutMapping("/categories/{categoryId}")
    @Operation(summary = "카테고리명 수정", description = "기존 카테고리명을 수정합니다.")
    @SecurityRequirement(name = "bearerAuth")
    public BaseResponse<Void> modifyCategory(
            @PathVariable Long categoryId,
            @RequestBody CategoryRequest request,
            Principal principal
    ) {
        log.info("Admin '{}' is modifying category ID '{}' to new name '{}'", principal.getName(), categoryId, request.getCategoryName());
        categoryService.modifyCategory(categoryId, request, principal.getName());
        return BaseResponse.ok(SuccessCode.CATEGORY_MODIFY_SUCCESS);
    }

    @DeleteMapping("/categories/{categoryId}")
    @Operation(summary = "카테고리 삭제", description = "기존 카테고리를 삭제합니다.")
    @SecurityRequirement(name = "bearerAuth")
    public BaseResponse<Void> deleteCategory(
            @PathVariable Long categoryId,
            Principal principal
    ) {
        log.info("Admin '{}' is deleting category ID '{}'", principal.getName(), categoryId);
        categoryService.deleteCategory(categoryId, principal.getName());
        return BaseResponse.ok(SuccessCode.CATEGORY_DELETE_SUCCESS);
    }

    @GetMapping("/members")
    @Operation(summary = "회원 목록 조회", description = "전체 회원 목록을 조회합니다.")
    @SecurityRequirement(name = "bearerAuth")
    public BaseResponse<PageMemberDto<MemberDetailResponse>> getAllMembers(
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "20") int size,
            Principal principal
    ) {
        PageMemberDto<MemberDetailResponse> members = memberService.getAllMembers(principal.getName(), page, size);
        return BaseResponse.ok(SuccessCode.MEMBER_LIST_DETAIL_SUCCESS, members);
    }

    @PatchMapping("/members/{memberId}")
    @Operation(summary = "회원 상태 변경", description = "특정 회원의 활동을 변경합니다.")
    @SecurityRequirement(name = "bearerAuth")
    public BaseResponse<Void> setMemberStatus(
            @PathVariable Long memberId,
            @RequestBody MemberStatusRequest request,
            Principal principal
    ) {
        log.info("Admin '{}' is changing status of member ID '{}' to '{}'", principal.getName(), memberId, request.getStatus());
        memberService.setMemberStatus(memberId, request, principal.getName());
        return BaseResponse.ok(SuccessCode.MEMBER_STATUS_CHANGE_SUCCESS);
    }
}
