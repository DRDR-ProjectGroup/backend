package com.dorandoran.domain.post.repository;

import com.dorandoran.domain.member.entity.Member;
import com.dorandoran.domain.post.entity.Post;
import com.dorandoran.domain.post.entity.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    Optional<PostLike> findByMemberAndPost(Member member, Post post);
}
