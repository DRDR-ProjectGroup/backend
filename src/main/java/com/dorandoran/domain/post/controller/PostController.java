package com.dorandoran.domain.post.controller;

import com.dorandoran.domain.post.dto.request.PostCreateRequest;
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
    public BaseResponse<Void> createPost(
            @PathVariable String categoryName,
            @RequestPart("post") PostCreateRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            Principal principal
    ) throws IOException {
        postService.createPost(principal.getName(), categoryName, request, files);
        return BaseResponse.ok(SuccessCode.POST_CREATE_SUCCESS);
    }

}
