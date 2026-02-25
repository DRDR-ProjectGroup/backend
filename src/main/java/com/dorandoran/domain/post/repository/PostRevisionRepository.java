package com.dorandoran.domain.post.repository;

import com.dorandoran.domain.post.entity.PostRevision;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRevisionRepository extends JpaRepository<PostRevision, Long> {
}
