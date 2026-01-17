package com.dorandoran.domain.comment.service;

import com.dorandoran.domain.comment.dto.request.CommentModifyRequest;
import com.dorandoran.domain.comment.dto.request.CommentRequest;
import com.dorandoran.domain.comment.dto.response.CommentListResponse;
import com.dorandoran.domain.comment.entity.Comment;
import com.dorandoran.domain.comment.repository.CommentRepository;
import com.dorandoran.domain.member.entity.Member;
import com.dorandoran.domain.member.service.MemberService;
import com.dorandoran.domain.post.entity.Post;
import com.dorandoran.domain.post.service.PostService;
import com.dorandoran.global.exception.CustomException;
import com.dorandoran.global.response.ErrorCode;
import com.dorandoran.standard.page.dto.PageCommentDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final MemberService memberService;
    private final PostService postService;

    @Transactional
    public void createComment(Long postId, CommentRequest request, String memberId) {
        Member member = memberService.findMemberByStringId(memberId);

        Post post = postService.findPostById(postId);

        Comment parentComment = findCommentById(request.getParentCommentId());

        Comment newComment = Comment.createComment(post, member, parentComment, request.getContent());

        commentRepository.save(newComment);
    }

    @Transactional(readOnly = true)
    public PageCommentDto<CommentListResponse> getComments(Long postId, int page, int size) {
        Post post = postService.findPostById(postId);

        Pageable pageable = PageRequest.of(Math.max(0, page - 1), size, Sort.by(Sort.Order.asc("createdAt")));

        // 1. 부모 댓글 (0depth) 페이징 조회
        Page<Comment> parentPage = commentRepository.findByPostAndDepth(post, 0, pageable);
        List<Comment> parents = parentPage.getContent();

        // 부모 댓글이 없으면 (댓글이 없으면) 빈 페이지 반환
        if (parents.isEmpty()) {
            return new PageCommentDto<>(parentPage.map(comment -> CommentListResponse.of(comment, Collections.emptyList())));
        }

        // 2. 게시글의 모든 댓글을 한 번에 가져와서 메모리에서 트리 구성 (N+1 방지)
        List<Comment> allComments = commentRepository.findAllByPost(post);

        // 3. 부모-자식 매핑 생성
        Map<Long, List<Comment>> childrenMap = new HashMap<>();

        for (Comment comment : allComments) {
            // 부모 댓글이 있는 경우에만 매핑 추가
            if (comment.getParentComment() != null) {
                // 자식 댓글에서 부모 댓글 ID 얻기
                Long parentCommentId = comment.getParentComment().getId();

                // 부모 ID를 키로 자식 댓글 리스트에 추가 (없으면 빈 리스트 생성)
                childrenMap.computeIfAbsent(parentCommentId, key -> new ArrayList<>()).add(comment);
            }
        }

        // 4. 각 부모에 대해 재귀적으로 트리 생성
        List<CommentListResponse> pageResponses = parents.stream()
                .map(parent -> buildTree(parent, childrenMap))
                .collect(Collectors.toList());

        // 5. PageImpl으로 페이지 메타정보 유지하면서 반환
        Page<CommentListResponse> responsePage = new PageImpl<>(pageResponses, pageable, parentPage.getTotalElements());
        return new PageCommentDto<>(responsePage);
    }

    @Transactional
    public void modifyComment(Long postId, Long commentId, CommentModifyRequest request, String memberId) {
        Comment comment = findCommentAndCheckAuthority(postId, commentId, memberId);

        comment.modifyContent(request.getContent());
    }

    @Transactional
    public void deleteComment(Long postId, Long commentId, String memberId) {
        Comment comment = findCommentAndCheckAuthority(postId, commentId, memberId);

        comment.delete();
    }

    // 댓글 트리를 위한 재귀 함수
    private CommentListResponse buildTree(Comment comment, Map<Long, List<Comment>> childrenMap) {
        List<Comment> directChildren = childrenMap.getOrDefault(comment.getId(), Collections.emptyList());

        List<CommentListResponse> childResponses = directChildren.stream()
                .sorted(Comparator.comparing(Comment::getCreatedAt))
                .map(child -> buildTree(child, childrenMap))
                .collect(Collectors.toList());

        return CommentListResponse.of(comment, childResponses);
    }

    public Comment findCommentById(Long commentId) {
        if (commentId == null) return null;

        return commentRepository.findById(commentId)
                .orElse(null);
    }

    private Comment findCommentAndCheckAuthority(Long postId, Long commentId, String memberId) {
        Member member = memberService.findMemberByStringId(memberId);

        Post post = postService.findPostById(postId);

        Comment comment = commentRepository.findByIdAndPost(commentId, post)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

        // 작성자 권한 확인
        if (!comment.getMember().getId().equals(member.getId())) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        return comment;
    }
}
