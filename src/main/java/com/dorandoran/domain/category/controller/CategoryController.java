package com.dorandoran.domain.category.controller;

import com.dorandoran.domain.category.dto.response.CategoryGroupResponse;
import com.dorandoran.domain.category.service.CategoryService;
import com.dorandoran.global.response.BaseResponse;
import com.dorandoran.global.response.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    @Operation(summary = "카테고리 목록 조회", description = "모든 카테고리 목록을 조회합니다.")
    public BaseResponse<List<CategoryGroupResponse>> getCategories() {

        List<CategoryGroupResponse> categoryList = categoryService.getCategories();

        return BaseResponse.ok(SuccessCode.CATEGORY_LIST_SUCCESS, categoryList);
    }
}
