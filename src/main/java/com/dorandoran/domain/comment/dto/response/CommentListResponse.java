package com.dorandoran.domain.comment.dto.response;

import com.dorandoran.domain.comment.entity.Comment;
import com.dorandoran.domain.member.dto.response.AuthorResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentListResponse {
    private Long commentId;
    private AuthorResponse author;
    private String content;
    private LocalDateTime createdAt;
    private List<CommentListResponse> child;

    public static CommentListResponse of(Comment comment, List<CommentListResponse> childComments) {
        boolean deleted = comment.getDeletedAt() != null;
        return CommentListResponse.builder()
                .commentId(comment.getId())
                .author(deleted ? null : AuthorResponse.of(comment.getMember()))
                .content(deleted ? "삭제된 댓글입니다." : comment.getContent())
                .createdAt(comment.getCreatedAt())
                .child(childComments)
                .build();
    }
}
