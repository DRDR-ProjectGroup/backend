package com.dorandoran.domain.category.entity;

import com.dorandoran.global.jpa.entity.BaseTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CategoryGroup extends BaseTime {

    @Column(nullable = false, unique = true)
    private String name;

    @Builder
    private CategoryGroup(String name) {
        this.name = name;
    }

    public static CategoryGroup createCategoryGroup(String name) {
        return CategoryGroup.builder()
                .name(name)
                .build();
    }

    public void modifyGroupName(String groupName) {
        this.name = groupName;
    }
}
