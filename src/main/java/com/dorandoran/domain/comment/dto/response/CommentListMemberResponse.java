package com.dorandoran.domain.comment.dto.response;

import com.dorandoran.domain.comment.entity.Comment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentListMemberResponse {
    private Long postId;
    private String content;
    private LocalDateTime createdAt;

    public static CommentListMemberResponse of(Comment comment) {
        return CommentListMemberResponse.builder()
                .postId(comment.getPost().getId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
