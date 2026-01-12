package com.dorandoran.domain.post.controller;

import com.dorandoran.domain.post.dto.request.PostCreateRequest;
import com.dorandoran.domain.post.dto.response.PostResponse;
import com.dorandoran.domain.post.service.PostService;
import com.dorandoran.global.response.BaseResponse;
import com.dorandoran.global.response.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/posts")
@Tag(name = "PostController", description = "게시글 관련 API")
public class PostController {

    private final PostService postService;

    @PostMapping(path = "/{categoryName}", consumes = {"multipart/form-data"})
    @Operation(summary = "게시글 생성", description = "카테고리 이름에 해당하는 게시글을 생성합니다.")
    @SecurityRequirement(name = "bearerAuth")
    public BaseResponse<PostResponse> createPost(
            @PathVariable String categoryName,
            @RequestPart("post") PostCreateRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            Principal principal
    ) throws IOException {
        PostResponse postResponse = postService.createPost(principal.getName(), categoryName, request, files);
        return BaseResponse.ok(SuccessCode.POST_CREATE_SUCCESS, postResponse);
    }

    @GetMapping("/{postId}")
    @Operation(summary = "게시글 조회", description = "ID에 해당하는 게시글을 조회합니다.")
    public BaseResponse<PostResponse> getPost(
            @PathVariable Long postId, Principal principal,
            @CookieValue(name = "GuestToken", required = false) String guestToken
    ) {
        PostResponse postResponse = postService.getPostById(postId, principal != null ? principal.getName() : guestToken);
        return BaseResponse.ok(SuccessCode.POST_DETAIL_SUCCESS, postResponse);
    }

    @PutMapping(path = "/{postId}", consumes = {"multipart/form-data"})
    @Operation(summary = "게시글 수정", description = "ID에 해당하는 게시글을 수정합니다.")
    @SecurityRequirement(name = "bearerAuth")
    public BaseResponse<PostResponse> modifyPost(
            @PathVariable Long postId,
            @RequestPart("post") PostCreateRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            Principal principal
    ) throws IOException {
        PostResponse postResponse = postService.modifyPost(principal.getName(), postId, request, files);
        return BaseResponse.ok(SuccessCode.POST_MODIFY_SUCCESS, postResponse);
    }
}
