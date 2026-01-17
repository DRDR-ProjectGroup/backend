package com.dorandoran.factory;

import com.dorandoran.domain.comment.entity.Comment;
import com.dorandoran.domain.comment.repository.CommentRepository;
import com.dorandoran.domain.member.entity.Member;
import com.dorandoran.domain.post.entity.Post;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public final class CommentFactory {

    private final EntityManager em;
    private final CommentRepository commentRepository;

    public Comment saveAndCreateComment(Post post, Member member, Comment parentCommentId, String content) {
        Comment newComment = Comment.createComment(post, member, parentCommentId, content);

        Comment savedComment = commentRepository.save(newComment);

        flushAndClear();

        return savedComment;
    }

    private void flushAndClear() {
        em.flush();
        em.clear();
    }
}
