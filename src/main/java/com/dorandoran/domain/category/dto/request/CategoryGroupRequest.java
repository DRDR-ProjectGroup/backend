package com.dorandoran.domain.category.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryGroupRequest {
    @NotBlank(message = "그룹 이름은 필수 입력 항목입니다.")
    private String groupName;
}
