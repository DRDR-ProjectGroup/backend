package com.dorandoran.factory;

import com.dorandoran.domain.category.entity.CategoryGroup;
import com.dorandoran.domain.category.repository.CategoryGroupRepository;
import com.dorandoran.domain.category.repository.CategoryRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CategoryGroupFactory {

    private final CategoryGroupRepository categoryGroupRepository;
    private final CategoryRepository categoryRepository;
    private final EntityManager em;

    public List<CategoryGroup> saveAndCreateDefaultCategoryGroup() {
        List<CategoryGroup> savedGroups = new ArrayList<>();
        savedGroups.add(categoryGroupRepository.save(CategoryGroup.createCategoryGroup("일반")));
        savedGroups.add(categoryGroupRepository.save(CategoryGroup.createCategoryGroup("유머")));
        savedGroups.add(categoryGroupRepository.save(CategoryGroup.createCategoryGroup("정보")));
        savedGroups.add(categoryGroupRepository.save(CategoryGroup.createCategoryGroup("게임")));

        flushAndClear();
        return savedGroups;
    }

    private void flushAndClear() {
        em.flush();
        em.clear();
    }
}
