package com.dorandoran.domain.category.entity;

import com.dorandoran.global.jpa.entity.BaseTime;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category extends BaseTime {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private CategoryGroup group;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String address;

    @Builder
    private Category(CategoryGroup group, String name, String address) {
        this.group = group;
        this.name = name;
        this.address = address;
    }

    public static Category createCategory(CategoryGroup group, String name, String address) {
        return Category.builder()
                .group(group)
                .name(name)
                .address(address)
                .build();
    }

    public void modifyCategory(CategoryGroup group, String name, String address) {
        this.group = group;
        this.name = name;
        this.address = address;
    }
}
