package com.dorandoran.domain.category.repository;

import com.dorandoran.domain.category.entity.CategoryGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryGroupRepository extends JpaRepository<CategoryGroup, Long> {

    Optional<CategoryGroup> findByName(String name);
}
