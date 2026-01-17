package com.dorandoran.domain.comment.dto.response;

import com.dorandoran.domain.comment.entity.Comment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentListMemberResponse {
    private String postId;
    private String content;
    private String createdAt;

    public static CommentListMemberResponse of(Comment comment) {
        return CommentListMemberResponse.builder()
                .postId(comment.getPost().getId().toString())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt().toString())
                .build();
    }
}
