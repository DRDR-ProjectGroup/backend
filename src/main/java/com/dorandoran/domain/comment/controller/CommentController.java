package com.dorandoran.domain.comment.controller;

import com.dorandoran.domain.comment.dto.request.CommentModifyRequest;
import com.dorandoran.domain.comment.dto.request.CommentRequest;
import com.dorandoran.domain.comment.dto.response.CommentListResponse;
import com.dorandoran.domain.comment.service.CommentService;
import com.dorandoran.global.response.BaseResponse;
import com.dorandoran.global.response.SuccessCode;
import com.dorandoran.standard.page.dto.PageCommentDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/posts/{postId}/comments")
@Tag(name = "CommentController", description = "댓글 관련 API")
@Slf4j
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    @Operation(summary = "댓글 생성")
    @SecurityRequirement(name = "bearerAuth")
    public BaseResponse<Void> createComment(
            @PathVariable Long postId,
            @RequestBody CommentRequest request,
            Principal principal
    ) {
        log.info("Creating comment for postId: {}, by user: {}", postId, principal.getName());
        commentService.createComment(postId, request, principal.getName());
        return BaseResponse.ok(SuccessCode.COMMENT_CREATE_SUCCESS);
    }

    @GetMapping
    @Operation(summary = "댓글 조회")
    public BaseResponse<PageCommentDto<CommentListResponse>> getComments(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageCommentDto<CommentListResponse> comments = commentService.getComments(postId, page, size);
        return BaseResponse.ok(SuccessCode.COMMENT_LIST_READ_SUCCESS, comments);
    }

    @PatchMapping("/{commentId}")
    @Operation(summary = "댓글 수정")
    @SecurityRequirement(name = "bearerAuth")
    public BaseResponse<Void> modifyComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @RequestBody CommentModifyRequest request,
            Principal principal
    ) {
        log.info("Modifying commentId: {} for postId: {}, by user: {}", commentId, postId, principal.getName());
        commentService.modifyComment(postId, commentId, request, principal.getName());
        return BaseResponse.ok(SuccessCode.COMMENT_MODIFY_SUCCESS);
    }

    @DeleteMapping("/{commentId}")
    @Operation(summary = "댓글 삭제")
    @SecurityRequirement(name = "bearerAuth")
    public BaseResponse<Void> deleteComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            Principal principal
    ) {
        log.info("Deleting commentId: {} for postId: {}, by user: {}", commentId, postId, principal.getName());
        commentService.deleteComment(postId, commentId, principal.getName());
        return BaseResponse.ok(SuccessCode.COMMENT_DELETE_SUCCESS);
    }
}
