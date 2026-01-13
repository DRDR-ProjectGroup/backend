package com.dorandoran.domain.post.repository;

import com.dorandoran.domain.category.entity.Category;
import com.dorandoran.domain.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Modifying
    @Query("UPDATE Post p SET p.viewCount = p.viewCount + 1 WHERE p.id = :postId")
    void incrementViewCount(@Param("postId") Long postId);

    // 기본 전체 조회 (카테고리 + 삭제안된 게시글)
    Page<Post> findByCategoryAndDeletedAtIsNull(Category category, Pageable pageable);

    // 검색별 메서드들
    Page<Post> findByCategoryAndTitleContainingAndDeletedAtIsNull(Category category, String title, Pageable pageable);

    Page<Post> findByCategoryAndContentContainingAndDeletedAtIsNull(Category category, String content, Pageable pageable);

    Page<Post> findByCategoryAndMember_NicknameContainingAndDeletedAtIsNull(Category category, String nickname, Pageable pageable);

    // 전체(제목 OR 내용 OR 작성자)
    @Query("SELECT p FROM Post p WHERE p.category = :category AND p.deletedAt IS NULL AND (p.title LIKE %:kw% OR p.content LIKE %:kw% OR p.member.nickname LIKE %:kw%)")
    Page<Post> searchAllInCategory(@Param("category") Category category, @Param("kw") String keyword, Pageable pageable);
}
