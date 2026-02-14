package com.dorandoran.domain.comment.dto.response;

import com.dorandoran.domain.comment.entity.Comment;
import com.dorandoran.domain.member.type.MemberStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentParentResponse {
    private Long commentId;
    private long memberId;
    private String nickname;
    private MemberStatus status;

    public static CommentParentResponse of(Comment comment) {
        return CommentParentResponse.builder()
                .commentId(comment.getParentComment().getId())
                .memberId(comment.getParentComment().getMember().getId())
                .nickname(comment.getParentComment().getMember().getNickname())
                .status(comment.getParentComment().getMember().getStatus())
                .build();
    }
}
