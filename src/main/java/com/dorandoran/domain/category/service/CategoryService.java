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
import com.dorandoran.domain.post.service.PostService;
import com.dorandoran.global.exception.CustomException;
import com.dorandoran.global.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final MemberService memberService;
    private final PostService postService;
    private final CategoryRepository categoryRepository;
    private final CategoryGroupRepository categoryGroupRepository;

    @Transactional(readOnly = true)
    public List<CategoryGroupResponse> getCategories() {
        List<Category> categories = categoryRepository.findByDeletedAtIsNull();

        Map<Long, List<Category>> categoryMap = categories.stream()
                .collect(Collectors.groupingBy(
                        category -> category.getGroup().getId()
                ));

        List<CategoryGroup> groups = categoryGroupRepository.findByDeletedAtIsNull();

        List<CategoryGroupResponse> responses = groups.stream()
                .map(group -> {
                    List<CategoryResponse> categoryResponses =
                            categoryMap
                                    .getOrDefault(group.getId(), List.of())
                                    .stream()
                                    .map(CategoryResponse::of)
                                    .toList();

                    return CategoryGroupResponse.of(group, categoryResponses);
                })
                .toList();

        return responses;
    }

    @Transactional
    public void createCategoryGroup(CategoryGroupRequest request, String memberId) {
        validateAdmin(memberId);

        if (categoryGroupRepository.existsByName(request.getGroupName())) {
            throw new CustomException(ErrorCode.CATEGORY_GROUP_DUPLICATE_NAME);
        }

        CategoryGroup categoryGroup = CategoryGroup.createCategoryGroup(request.getGroupName());

        categoryGroupRepository.save(categoryGroup);
    }

    @Transactional
    public void modifyCategoryGroup(Long groupId, CategoryGroupRequest request, String memberId) {
        validateAdmin(memberId);

        if (categoryGroupRepository.existsByName(request.getGroupName())) {
            throw new CustomException(ErrorCode.CATEGORY_GROUP_DUPLICATE_NAME);
        }

        CategoryGroup categoryGroup = categoryGroupRepository.findById(groupId)
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_GROUP_NOT_FOUND));

        categoryGroup.modifyGroupName(request.getGroupName());
    }

    @Transactional
    public void deleteCategoryGroup(Long groupId, String memberId) {
        validateAdmin(memberId);

        CategoryGroup categoryGroup = categoryGroupRepository.findById(groupId)
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_GROUP_NOT_FOUND));

        // 카테고리의 deletedAt이 null인 것들만 조회
        List<Category> categories = categoryRepository.findByGroupIdAndDeletedAtIsNull(groupId);

        if (!categories.isEmpty()) {
            throw new CustomException(ErrorCode.CATEGORY_GROUP_DELETE_FAIL_HAS_CATEGORIES);
        }

        categoryGroup.setDeletedAt();
    }

    @Transactional
    public void createCategory(CategoryRequest request, String memberId) {
        validateAdmin(memberId);

        if (categoryRepository.existsByName(request.getCategoryName()) || categoryRepository.existsByAddress(request.getCategoryAddress())) {
            throw new CustomException(ErrorCode.CATEGORY_DUPLICATE);
        }

        CategoryGroup categoryGroup = categoryGroupRepository.findById(request.getGroupId())
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_GROUP_NOT_FOUND));

        Category category = Category.createCategory(categoryGroup, request.getCategoryName(), request.getCategoryAddress());

        categoryRepository.save(category);
    }

    @Transactional
    public void modifyCategory(Long categoryId, CategoryRequest request, String memberId) {
        validateAdmin(memberId);

        if (categoryRepository.existsByName(request.getCategoryName()) || categoryRepository.existsByAddress(request.getCategoryAddress())) {
            throw new CustomException(ErrorCode.CATEGORY_DUPLICATE);
        }

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

        // 카테고리에 게시글이 있으면 삭제 불가
        if (postService.existsNotDeletedPostByCategoryId(categoryId)) {
            throw new CustomException(ErrorCode.CATEGORY_DELETE_FAIL_HAS_POSTS);
        }

        category.setDeletedAt();
    }

    private void validateAdmin(String memberId) {
        Member member = memberService.findMemberByStringId(memberId);

        if (!member.isAdmin()) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
    }
}
