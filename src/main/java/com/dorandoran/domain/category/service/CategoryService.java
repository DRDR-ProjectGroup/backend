package com.dorandoran.domain.category.service;

import com.dorandoran.domain.category.dto.response.CategoryGroupResponse;
import com.dorandoran.domain.category.dto.response.CategoryResponse;
import com.dorandoran.domain.category.entity.Category;
import com.dorandoran.domain.category.entity.CategoryGroup;
import com.dorandoran.domain.category.repository.CategoryGroupRepository;
import com.dorandoran.domain.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryGroupRepository categoryGroupRepository;

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
}
