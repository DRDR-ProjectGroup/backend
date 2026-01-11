package com.dorandoran.factory;

import com.dorandoran.domain.category.entity.Category;
import com.dorandoran.domain.category.entity.CategoryGroup;
import com.dorandoran.domain.category.repository.CategoryGroupRepository;
import com.dorandoran.domain.category.repository.CategoryRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CategoryFactory {

    private final CategoryGroupRepository categoryGroupRepository;
    private final CategoryRepository categoryRepository;
    private final EntityManager em;

    public Category saveAndCreateCategory(String groupName, String categoryName) {
        CategoryGroup categoryGroup = categoryGroupRepository.findByName(groupName).orElse(null);
        Category saved = categoryRepository.save(Category.createCategory(categoryGroup, categoryName, categoryName));

        flushAndClear();
        return saved;
    }

    private void flushAndClear() {
        em.flush();
        em.clear();
    }
}
