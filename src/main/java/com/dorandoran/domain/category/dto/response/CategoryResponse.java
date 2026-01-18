package com.dorandoran.domain.category.dto.response;

import com.dorandoran.domain.category.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryResponse {

    private String categoryName;
    private String categoryAddress;

    public static CategoryResponse of(Category category) {
        return CategoryResponse.builder()
                .categoryName(category.getName())
                .categoryAddress(category.getAddress())
                .build();
    }
}
