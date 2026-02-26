package com.dorandoran.domain.comment.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CommentModifyRequest {
    @NotBlank(message = "댓글 내용을 입력해주세요.")
    private String content;
}
