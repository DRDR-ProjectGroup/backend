package com.dorandoran.domain.post.controller;

import com.dorandoran.domain.post.dto.request.PostCreateRequest;
import com.dorandoran.domain.post.dto.request.PostLikeRequest;
import com.dorandoran.domain.post.dto.response.PostLikeResponse;
import com.dorandoran.domain.post.dto.response.PostListResponse;
import com.dorandoran.domain.post.dto.response.PostResponse;
import com.dorandoran.domain.post.service.PostService;
import com.dorandoran.domain.post.type.PostSortType;
import com.dorandoran.global.response.BaseResponse;
import com.dorandoran.global.response.SuccessCode;
import com.dorandoran.standard.page.dto.PageDto;
import com.dorandoran.standard.search.SearchType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/posts")
@Tag(name = "PostController", description = "게시글 관련 API")
@Slf4j
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

    @DeleteMapping("/{postId}")
    @Operation(summary = "게시글 삭제", description = "ID에 해당하는 게시글을 삭제합니다.")
    @SecurityRequirement(name = "bearerAuth")
    public BaseResponse<Void> deletePost(
            @PathVariable Long postId,
            Principal principal
    ) {
        postService.deletePost(postId, principal.getName());
        return BaseResponse.ok(SuccessCode.POST_DELETE_SUCCESS);
    }

    @GetMapping
    @Operation(summary = "게시글 목록 조회", description = "카테고리별 게시글 목록을 조회합니다.")
    public BaseResponse<PageDto<PostListResponse>> getPostsByCategory(
            @RequestParam(value = "cat", required = false) String categoryName,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(value = "searchTarget", required = false) SearchType searchType,
            @RequestParam(value = "searchKeyword", required = false) String keyword,
            @RequestParam(defaultValue = "LATEST", required = false) PostSortType sort
    ) {
        PageDto<PostListResponse> postsPage = postService.getPostsByCategory(categoryName, searchType, keyword, page, size, sort);
        return BaseResponse.ok(SuccessCode.POST_LIST_SUCCESS, postsPage);
    }

    @PostMapping("/{postId}/like")
    @Operation(summary = "게시글 추천, 비추천", description = "ID에 해당하는 게시글을 추천/비추천합니다.")
    @SecurityRequirement(name = "bearerAuth")
    public BaseResponse<PostLikeResponse> likePost(
            @PathVariable Long postId,
            @RequestBody PostLikeRequest request,
            Principal principal
    ) {
        PostLikeResponse response = postService.likePost(principal.getName(), postId, request);
        return BaseResponse.ok(SuccessCode.POST_LIKE_SUCCESS, response);
    }

    @PostMapping("/{postId}/notice")
    @Operation(summary = "게시글 공지 설정/해제", description = "관리자가 ID에 해당하는 게시글을 공지로 설정하거나 해제합니다.")
    @SecurityRequirement(name = "bearerAuth")
    public BaseResponse<Void> setPostNotice(
            @PathVariable Long postId,
            Principal principal
    ) {
        postService.setPostNotice(principal.getName(), postId);
        return BaseResponse.ok(SuccessCode.POST_NOTICE_SUCCESS);
    }
}
