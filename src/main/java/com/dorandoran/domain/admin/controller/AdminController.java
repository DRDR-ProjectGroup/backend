package com.dorandoran.domain.admin.controller;

import com.dorandoran.domain.category.dto.request.CategoryGroupRequest;
import com.dorandoran.domain.category.dto.request.CategoryRequest;
import com.dorandoran.domain.category.service.CategoryService;
import com.dorandoran.global.response.BaseResponse;
import com.dorandoran.global.response.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final CategoryService categoryService;

    @PostMapping("/groups")
    @Operation(summary = "카테고리 그룹 생성", description = "새로운 카테고리 그룹을 생성합니다.")
    @SecurityRequirement(name = "bearerAuth")
    public BaseResponse<Void> createCategoryGroup(
            @RequestBody CategoryGroupRequest request,
            Principal principal
    ) {
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
        categoryService.deleteCategory(categoryId, principal.getName());
        return BaseResponse.ok(SuccessCode.CATEGORY_DELETE_SUCCESS);
    }
}
