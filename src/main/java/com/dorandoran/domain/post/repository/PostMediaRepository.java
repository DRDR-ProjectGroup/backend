package com.dorandoran.domain.post.repository;

import com.dorandoran.domain.post.entity.PostMedia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostMediaRepository extends JpaRepository<PostMedia, Long> {
    Optional<PostMedia> findByIdAndPostId(Long mediaId, Long postId);
}
