package com.dorandoran.domain.category.repository;

import com.dorandoran.domain.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByName(String categoryName);

    Optional<Category> findByAddress(String address);

    List<Category> findByGroupId(Long groupId);

    boolean existsByName(String categoryName);

    boolean existsByAddress(String categoryAddress);

    List<Category> findByDeletedAtIsNull();

    List<Category> findByGroupIdAndDeletedAtIsNull(Long groupId);

    @Modifying
    @Query("DELETE FROM Category c WHERE c.deletedAt IS NOT NULL AND NOT EXISTS (SELECT p FROM Post p WHERE p.category = c)")
    void deleteOrphanedSoftDeletedCategories();
}
