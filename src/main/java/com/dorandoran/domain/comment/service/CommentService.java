package com.dorandoran.domain.comment.service;

import com.dorandoran.domain.comment.dto.request.CommentModifyRequest;
import com.dorandoran.domain.comment.dto.request.CommentRequest;
import com.dorandoran.domain.comment.dto.response.CommentCountResponse;
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

        // 응답에 사용할 pageable (0-based page 인덱스 사용)
        Pageable responsePageable = PageRequest.of(Math.max(0, page - 1), size, Sort.by(Sort.Order.asc("createdAt")));

        // 1) 게시글의 모든 댓글을 한 번에 가져와서 트리 구성용 매핑 생성
        List<Comment> allComments = commentRepository.findAllByPost(post);

        if (allComments.isEmpty()) {
            Page<CommentListResponse> empty = new PageImpl<>(Collections.emptyList(), responsePageable, 0);
            return new PageCommentDto<>(empty);
        }

        // 2) 부모-자식 매핑 생성
        Map<Long, List<Comment>> childrenMap = new HashMap<>();
        for (Comment c : allComments) {
            if (c.getParentComment() != null) {
                Long pid = c.getParentComment().getId();
                childrenMap.computeIfAbsent(pid, k -> new ArrayList<>()).add(c);
            }
        }

        // 3) 최상위 부모(루트) 리스트 생성 및 정렬
        List<Comment> topParents = allComments.stream()
                .filter(c -> c.getParentComment() == null)
                .sorted(Comparator.comparing(Comment::getCreatedAt))
                .toList();

        // 4) 전위 순회로 평탄화
        List<Comment> flatList = new ArrayList<>();
        for (Comment root : topParents) {
            flattenPreOrder(root, childrenMap, flatList);
        }

        int total = flatList.size();
        int requestedPage = Math.max(1, page);
        int offset = Math.max(0, requestedPage - 1) * size;

        if (offset >= total) {
            Page<CommentListResponse> empty = new PageImpl<>(Collections.emptyList(), responsePageable, total);
            return new PageCommentDto<>(empty);
        }

        int start = offset;
        int end = Math.min(offset + size, total);

        // 인덱스 맵 생성
        Map<Long, Integer> indexById = new HashMap<>();
        for (int i = 0; i < flatList.size(); i++) {
            indexById.put(flatList.get(i).getId(), i);
        }

        // 5) 페이징 범위에 포함된 댓글들의 조상들이 모두 포함되도록 start 조정
        int adjustedStart = start;
        for (int i = start; i < end; i++) {
            Comment c = flatList.get(i);
            Comment parent = c.getParentComment();
            while (parent != null) {
                Integer idx = indexById.get(parent.getId());
                if (idx == null) break;
                if (idx < adjustedStart) adjustedStart = idx;
                parent = parent.getParentComment();
            }
        }

        start = adjustedStart;
        end = Math.min(start + size, total);

        List<Comment> pageSlice = flatList.subList(start, end);

        Set<Long> includedIds = pageSlice.stream().map(Comment::getId).collect(Collectors.toSet());

        // 6) 포함된 댓글들로 childrenMap 필터링 (재구성용)
        Map<Long, List<Comment>> filteredChildrenMap = new HashMap<>();
        for (Map.Entry<Long, List<Comment>> entry : childrenMap.entrySet()) {
            List<Comment> filtered = entry.getValue().stream()
                    .filter(c -> includedIds.contains(c.getId()))
                    .sorted(Comparator.comparing(Comment::getCreatedAt))
                    .collect(Collectors.toList());
            if (!filtered.isEmpty()) {
                filteredChildrenMap.put(entry.getKey(), filtered);
            }
        }

        // 7) 루트 후보: 포함된 댓글 중 부모가 없거나 부모가 포함되지 않은 것
        List<Comment> roots = pageSlice.stream()
                .filter(c -> c.getParentComment() == null || !includedIds.contains(c.getParentComment().getId()))
                .sorted(Comparator.comparingInt(c -> indexById.get(c.getId())))
                .collect(Collectors.toList());

        List<CommentListResponse> pageResponses = roots.stream()
                .map(root -> buildTree(root, filteredChildrenMap))
                .collect(Collectors.toList());

        // 8) 응답 페이지 생성
        int responsePageIndex = start / size; // adjusted page index
        Pageable pageableForResponse = PageRequest.of(responsePageIndex, size, Sort.by(Sort.Order.asc("createdAt")));
        Page<CommentListResponse> responsePage = new PageImpl<>(pageResponses, pageableForResponse, total);

        return new PageCommentDto<>(responsePage);
    }
    
    // 댓글 트리를 위한 재귀 함수
    private CommentListResponse buildTree(Comment comment, Map<Long, List<Comment>> childrenMap) {
        List<Comment> directChildren = childrenMap.getOrDefault(comment.getId(), Collections.emptyList());

        List<CommentListResponse> childResponses = directChildren.stream()
                .sorted(Comparator.comparing(Comment::getCreatedAt))
                .map(child -> buildTree(child, childrenMap))
                .toList();

        return CommentListResponse.of(comment, childResponses);
    }

    // 전위 순회로 평탄화
    private void flattenPreOrder(Comment comment, Map<Long, List<Comment>> childrenMap, List<Comment> flatList) {
        flatList.add(comment);
        List<Comment> children = childrenMap.getOrDefault(comment.getId(), Collections.emptyList());
        if (!children.isEmpty()) {
            children.sort(Comparator.comparing(Comment::getCreatedAt));
            for (Comment c : children) {
                flattenPreOrder(c, childrenMap, flatList);
            }
        }
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

        // 작성자 권한 확인 & 관리자 권한 확인
        if (!comment.getMember().getId().equals(member.getId()) && !member.isAdmin()) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        return comment;
    }

    @Transactional(readOnly = true)
    public CommentCountResponse getCommentCount(Long postId) {
        Post post = postService.findPostById(postId);
        Long CommentCount = commentRepository.countByPost(post);
        return CommentCountResponse.builder()
                .postId(postId)
                .commentCount(CommentCount.intValue())
                .build();
    }
}
