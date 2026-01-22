package com.dorandoran.domain.category.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRequest {
    @NotNull(message = "그룹을 선택해주세요.")
    private Long groupId;
    @NotBlank(message = "카테고리 이름은 필수 입력 항목입니다.")
    private String categoryName;
    @NotBlank(message = "카테고리 주소는 필수 입력 항목입니다.")
    private String categoryAddress;
}
