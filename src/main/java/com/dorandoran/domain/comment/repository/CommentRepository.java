package com.dorandoran.domain.comment.repository;

import com.dorandoran.domain.comment.entity.Comment;
import com.dorandoran.domain.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Query("SELECT c FROM Comment c WHERE c.member = :findMember AND c.deletedAt IS NULL")
    Page<Comment> findAllByMember(Member findMember, Pageable pageable);
}
