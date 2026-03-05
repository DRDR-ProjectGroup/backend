package com.dorandoran.domain.category.repository;

import com.dorandoran.domain.category.entity.CategoryGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CategoryGroupRepository extends JpaRepository<CategoryGroup, Long> {

    Optional<CategoryGroup> findByName(String name);

    boolean existsByName(String groupName);

    List<CategoryGroup> findByDeletedAtIsNull();

    @Modifying
    @Query("DELETE FROM CategoryGroup cg WHERE cg.deletedAt IS NOT NULL AND NOT EXISTS (SELECT c FROM Category c WHERE c.group = cg)")
    void deleteOrphanedSoftDeletedCategoryGroups();
}
