package com.dorandoran.domain.post.repository;

import com.dorandoran.domain.category.entity.Category;
import com.dorandoran.domain.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Modifying
    @Query("UPDATE Post p SET p.viewCount = p.viewCount + 1 WHERE p.id = :postId")
    void incrementViewCount(@Param("postId") Long postId);

    @EntityGraph(attributePaths = {"member", "category"})
    @Query("""
            SELECT p FROM Post p
            WHERE (:category IS NULL OR p.category = :category)
              AND p.deletedAt IS NULL
              AND (:keyword IS NULL OR
                   (:searchType = 'TITLE' AND p.title LIKE %:keyword%) OR
                   (:searchType = 'CONTENT' AND p.content LIKE %:keyword%) OR
                   (:searchType = 'AUTHOR' AND p.member.nickname LIKE %:keyword%) OR
                   (:searchType = 'ALL' AND (
                        p.title LIKE %:keyword%
                     OR p.content LIKE %:keyword%
                     OR p.member.nickname LIKE %:keyword%
                   ))
              )
              AND (:minLikeCount IS NULL OR p.likeCount >= :minLikeCount)
            """)
    Page<Post> searchByCondition(
            @Param("category") Category category,
            @Param("searchType") String searchType,
            @Param("keyword") String keyword,
            @Param("minLikeCount") Integer minLikeCount,
            Pageable pageable
    );
}
