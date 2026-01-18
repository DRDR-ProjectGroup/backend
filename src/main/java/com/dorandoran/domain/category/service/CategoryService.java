package com.dorandoran.domain.category.service;

import com.dorandoran.domain.category.dto.request.CategoryGroupRequest;
import com.dorandoran.domain.category.dto.request.CategoryRequest;
import com.dorandoran.domain.category.dto.response.CategoryGroupResponse;
import com.dorandoran.domain.category.dto.response.CategoryResponse;
import com.dorandoran.domain.category.entity.Category;
import com.dorandoran.domain.category.entity.CategoryGroup;
import com.dorandoran.domain.category.repository.CategoryGroupRepository;
import com.dorandoran.domain.category.repository.CategoryRepository;
import com.dorandoran.domain.member.entity.Member;
import com.dorandoran.domain.member.service.MemberService;
import com.dorandoran.global.exception.CustomException;
import com.dorandoran.global.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final MemberService memberService;
    private final CategoryRepository categoryRepository;
    private final CategoryGroupRepository categoryGroupRepository;

    @Transactional(readOnly = true)
    public List<CategoryGroupResponse> getCategories() {
        List<Category> categories = categoryRepository.findAll();

        List<CategoryGroup> groups = categoryGroupRepository.findAll();

        List<CategoryGroupResponse> categoryGroupResponses = groups.stream()
                .map(
                        group -> {
                            List<Category> groupedCategories = categories.stream()
                                    .filter(category -> category.getGroup().getId().equals(group.getId()))
                                    .toList();

                            List<CategoryResponse> categoryResponses = groupedCategories.stream()
                                    .map(CategoryResponse::of)
                                    .toList();

                            return CategoryGroupResponse.of(group, categoryResponses);
                        }
                ).toList();

        return categoryGroupResponses;
    }

    @Transactional
    public void createCategoryGroup(CategoryGroupRequest request, String memberId) {
        validateAdmin(memberId);

        CategoryGroup categoryGroup = CategoryGroup.createCategoryGroup(request.getGroupName());

        categoryGroupRepository.save(categoryGroup);
    }

    @Transactional
    public void modifyCategoryGroup(Long groupId, CategoryGroupRequest request, String memberId) {
        validateAdmin(memberId);

        CategoryGroup categoryGroup = categoryGroupRepository.findById(groupId)
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_GROUP_NOT_FOUND));

        categoryGroup.modifyGroupName(request.getGroupName());
    }

    @Transactional
    public void deleteCategoryGroup(Long groupId, String memberId) {
        validateAdmin(memberId);

        CategoryGroup categoryGroup = categoryGroupRepository.findById(groupId)
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_GROUP_NOT_FOUND));

        List<Category> categories = categoryRepository.findByGroupId(groupId);

        if (!categories.isEmpty()) {
            throw new CustomException(ErrorCode.CATEGORY_GROUP_DELETE_FAIL_HAS_CATEGORIES);
        }

        categoryGroupRepository.delete(categoryGroup);
    }

    @Transactional
    public void createCategory(CategoryRequest request, String memberId) {
        validateAdmin(memberId);

        CategoryGroup categoryGroup = categoryGroupRepository.findById(request.getGroupId())
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_GROUP_NOT_FOUND));

        Category category = Category.createCategory(categoryGroup, request.getCategoryName(), request.getCategoryAddress());

        categoryRepository.save(category);
    }

    @Transactional
    public void modifyCategory(Long categoryId, CategoryRequest request, String memberId) {
        validateAdmin(memberId);

        CategoryGroup categoryGroup = categoryGroupRepository.findById(request.getGroupId())
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_GROUP_NOT_FOUND));

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));

        category.modifyCategory(categoryGroup, request.getCategoryName(), request.getCategoryAddress());
    }

    @Transactional
    public void deleteCategory(Long categoryId, String memberId) {
        validateAdmin(memberId);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));

        categoryRepository.delete(category);
    }

    private void validateAdmin(String memberId) {
        Member member = memberService.findMemberByStringId(memberId);

        if (!member.isAdmin()) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
    }
}
