package com.dorandoran.domain.category.controller;

import com.dorandoran.SpringBootTestSupporter;
import com.dorandoran.domain.category.entity.Category;
import com.dorandoran.domain.category.entity.CategoryGroup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
class CategoryControllerTest extends SpringBootTestSupporter {

    private List<CategoryGroup> categoryGroups;
    private Category category;

    @BeforeEach
    void setUp() {
        categoryGroups = categoryGroupFactory.saveAndCreateDefaultCategoryGroup();
        category = categoryFactory.saveAndCreateCategory("게임", "lol");
    }

    @DisplayName("카테고리 목록 조회 테스트")
    @Test
    void getCategories() throws Exception {
        // given

        // when
        ResultActions result = mockMvc.perform(get("/api/v1/categories")
                .contentType("application/json")
        );

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].groupName").value(categoryGroups.get(0).getName()))
                .andExpect(jsonPath("$.data[3].categories[0].categoryName").value(category.getName()))
        ;
    }
}