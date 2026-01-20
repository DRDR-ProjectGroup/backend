package com.dorandoran.domain.category.dto.response;

import com.dorandoran.domain.category.entity.CategoryGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryGroupResponse {
    private long groupId;
    private String groupName;
    private List<CategoryResponse> categories;

    public static CategoryGroupResponse of(CategoryGroup categoryGroup, List<CategoryResponse> categories) {
        return CategoryGroupResponse.builder()
                .groupId(categoryGroup.getId())
                .groupName(categoryGroup.getName())
                .categories(categories)
                .build();
    }
}
