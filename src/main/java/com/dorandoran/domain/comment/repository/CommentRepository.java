package com.dorandoran.domain.comment.repository;

import com.dorandoran.domain.comment.entity.Comment;
import com.dorandoran.domain.member.entity.Member;
import com.dorandoran.domain.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Query("SELECT c FROM Comment c WHERE c.member = :findMember AND c.deletedAt IS NULL")
    Page<Comment> findAllByMember(Member findMember, Pageable pageable);

    @Query("SELECT c FROM Comment c WHERE c.post = :post AND c.depth = :depth ORDER BY c.createdAt ASC")
    Page<Comment> findByPostAndDepth(Post post, int depth, Pageable pageable);

    @Query("SELECT c FROM Comment c WHERE c.post = :post ORDER BY c.createdAt ASC")
    List<Comment> findAllByPost(Post post);

    Optional<Comment> findByIdAndPost(Long commentId, Post post);
}
