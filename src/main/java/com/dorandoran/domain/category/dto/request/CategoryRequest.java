package com.dorandoran.domain.category.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRequest {
    @NotBlank
    private Long groupId;
    @NotBlank
    private String categoryName;
    @NotBlank
    private String categoryAddress;
}
