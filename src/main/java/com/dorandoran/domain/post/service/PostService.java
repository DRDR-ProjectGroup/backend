package com.dorandoran.domain.post.service;

import com.dorandoran.domain.category.entity.Category;
import com.dorandoran.domain.category.repository.CategoryRepository;
import com.dorandoran.domain.member.entity.Member;
import com.dorandoran.domain.member.service.MemberService;
import com.dorandoran.domain.member.type.Role;
import com.dorandoran.domain.post.dto.request.PostCreateRequest;
import com.dorandoran.domain.post.dto.request.PostLikeRequest;
import com.dorandoran.domain.post.dto.response.PostLikeResponse;
import com.dorandoran.domain.post.dto.response.PostListResponse;
import com.dorandoran.domain.post.dto.response.PostMediaResponse;
import com.dorandoran.domain.post.dto.response.PostResponse;
import com.dorandoran.domain.post.entity.Post;
import com.dorandoran.domain.post.entity.PostLike;
import com.dorandoran.domain.post.entity.PostMedia;
import com.dorandoran.domain.post.repository.PostLikeRepository;
import com.dorandoran.domain.post.repository.PostRepository;
import com.dorandoran.domain.post.storage.MediaStorage;
import com.dorandoran.domain.post.storage.StoredMedia;
import com.dorandoran.domain.post.type.LikeType;
import com.dorandoran.domain.post.type.MediaType;
import com.dorandoran.domain.post.type.PostSortType;
import com.dorandoran.domain.search.doc.PostDocument;
import com.dorandoran.domain.search.dto.SearchResult;
import com.dorandoran.domain.search.service.PostIndexService;
import com.dorandoran.domain.search.service.PostSearchService;
import com.dorandoran.global.exception.CustomException;
import com.dorandoran.global.redis.RedisRepository;
import com.dorandoran.global.response.ErrorCode;
import com.dorandoran.standard.page.dto.PageDto;
import com.dorandoran.standard.search.SearchType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {

    private final PostRepository postRepository;
    private final MemberService memberService;
    private final CategoryRepository categoryRepository;
    private final MediaStorage mediaStorage;
    private final RedisRepository redisRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostIndexService postIndexService;
    private final PostSearchService postSearchService;

    @Value("${search.elastic.enabled}")
    private boolean elasticEnabled;

    private static final int POST_POPULAR_LIKE_COUNT = 10;

    @Transactional
    public PostResponse createPost(String memberId, String categoryName, PostCreateRequest request, List<MultipartFile> files) throws IOException {
        // 회원 조회
        Member member = memberService.findMemberByStringId(memberId);

        // 카테고리 조회
        Category category = categoryRepository.findByAddress(categoryName)
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));

        // 게시글 생성 로직 구현
        Post post = Post.createPost(member, category, request.getTitle(), request.getContent());

        // 우선 post를 저장해 부모 엔티티가 영속화된 상태로 만듦
        Post saved = postRepository.save(post);

        // 이미지 처리 로직 구현 (필요한 경우) - 부모가 영속 상태이므로, 미디어를 개별 저장
        if (files != null && !files.isEmpty()) {
            savePostMedia(saved, files);
        }

        // 검색을 위한 색인 작업 (미디어 저장 후, 트랜잭션 커밋 후 실행)
        PostDocument doc = PostDocument.createDoc(saved);
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    try {
                        postIndexService.index(doc);
                    } catch (IOException e) {
                        log.error("Failed to index post after commit, id={}", saved.getId(), e);
                    }
                }
            });
        } else {
            postIndexService.index(doc);
        }


        List<PostMediaResponse> mediaResponses = saved.getPostMediaList().stream()
                .map(PostMediaResponse::of)
                .toList();

        return PostResponse.of(saved, mediaResponses);
    }

    @Transactional
    public PostResponse getPostById(Long postId, String viewerIdentifier) {
        // 조회수 증가
        boolean viewed = redisRepository.hasViewedPost(postId, viewerIdentifier);

        if (!viewed) {
            postRepository.incrementViewCount(postId);

            redisRepository.setViewedPost(postId, viewerIdentifier);
        }

        Post post = findPostById(postId);

        List<PostMediaResponse> mediaResponses = post.getPostMediaList().stream()
                .map(PostMediaResponse::of)
                .toList();

        return PostResponse.of(post, mediaResponses);
    }

    @Transactional
    public PostResponse modifyPost(String memberId, Long postId, PostCreateRequest dto, List<MultipartFile> files) throws IOException {
        // 회원 조회
        Member member = memberService.findMemberByStringId(memberId);

        // 게시글 조회
        Post post = findPostById(postId);

        // 작성자 검증
        if (!post.getMember().getId().equals(member.getId())) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_POST_MODIFICATION);
        }

        // 게시글 수정 로직 구현
        post.modifyTitleAndContent(dto.getTitle(), dto.getContent());

        // 이미지 처리 로직 구현 (파일이 주어지면 기존 미디어를 교체)
        if (files != null && !files.isEmpty()) {
            // 기존 미디어 제거 (orphanRemoval=true 이므로 영속성 컨텍스트에 의해 삭제됨)
            post.clearMedia();

            // 새 미디어 추가
            savePostMedia(post, files);
        }

        // 수정 후 색인 갱신을 트랜잭션 커밋 후 실행 (DB 정합성을 위해 DB에 반영된 후 색인 작업 수행)
        PostDocument doc = PostDocument.createDoc(post);
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    try {
                        postIndexService.index(doc);
                    } catch (IOException e) {
                        log.error("Failed to index post after commit, id={}", post.getId(), e);
                    }
                }
            });
        } else {
            // 트랜잭션이 없으면 즉시 실행
            postIndexService.index(doc);
        }

        List<PostMediaResponse> mediaResponses = post.getPostMediaList().stream()
                .map(PostMediaResponse::of)
                .toList();

        return PostResponse.of(post, mediaResponses);
    }

    @Transactional
    public void deletePost(Long postId, String memberId) {
        // 회원 조회
        Member member = memberService.findMemberByStringId(memberId);

        // 게시글 조회
        Post post = findPostById(postId);

        // 작성자 본인 검증 및 관리자 권한 검증
        if (!post.getMember().getId().equals(member.getId()) && !member.isAdmin()) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_POST_MODIFICATION);
        }

        post.setDeletedAt();

        // soft-delete 후 ES 색인에서 해당 문서 삭제를 트랜잭션 커밋 후 실행
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    try {
                        postIndexService.delete(postId);
                    } catch (IOException e) {
                        log.error("Failed to delete post index after commit for id={}", postId, e);
                    }
                }
            });
        } else {
            try {
                postIndexService.delete(postId);
            } catch (IOException e) {
                log.error("Failed to delete post index for id={}", postId, e);
            }
        }
    }

    @Transactional(readOnly = true)
    public PageDto<PostListResponse> getPostsByCategory(String categoryName, SearchType searchType, String keyword, int page, int size, PostSortType sort) {
        Category category = null;

        if (categoryName != null) {
            category = categoryRepository.findByAddress(categoryName)
                    .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));
        }

        boolean useElastic =
                elasticEnabled && keyword != null && !keyword.isBlank();

        if (useElastic) {
            return searchByElasticSearch(searchType, keyword, page, size, sort, category);
        }

        return searchByDatabase(searchType, keyword, page, size, sort, category);
    }

    private PageDto<PostListResponse> searchByElasticSearch(SearchType searchType, String keyword, int page, int size, PostSortType sort, Category category) {
        // ES에서 id 목록과 총건수를 받아온다
        SearchResult result;

        try {
            result = postSearchService.searchPostIds(
                    searchType != null ? searchType : SearchType.ALL,
                    keyword,
                    category != null ? category.getAddress() : null,
                    (page - 1) * size,
                    size
            );
        } catch (IOException e) {
            throw new RuntimeException("Elasticsearch search failed", e);
        }

        List<Long> ids = result.getIds();
        long total = result.getTotalCount();

        log.debug("searchByElasticSearch -> ids={}, total={}", ids, total);

        if (ids == null || ids.isEmpty()) {
            Page<PostListResponse> emptyPage = new PageImpl<>(
                    List.<PostListResponse>of(),
                    PageRequest.of(Math.max(0, page - 1), size),
                    0L
            );
            return new PageDto<>(emptyPage, category);
        }

        // DB에서 해당 id들 조회
        List<Post> posts;
        if (sort == PostSortType.POPULAR) {
            posts = postRepository.findPopularPostsByIds(ids, POST_POPULAR_LIKE_COUNT);
        } else {
            posts = postRepository.findLatestPostsByIds(ids);
        }

        // createdAt 기준 내림차순으로 정렬하고 DTO로 변환
        List<PostListResponse> content = posts.stream()
                .map(PostListResponse::of)
                .toList();

        Page<PostListResponse> pageImpl = new PageImpl<>(
                content,
                PageRequest.of(Math.max(0, page - 1), size),
                total
        );

        return new PageDto<>(pageImpl, category);
    }

    private PageDto<PostListResponse> searchByDatabase(SearchType searchType, String keyword, int page, int size, PostSortType sort, Category category) {
        Pageable pageable = createPageable(page, size, sort);

        Integer minLikeCount = (sort == PostSortType.POPULAR) ? POST_POPULAR_LIKE_COUNT : null;
        String effectiveSearchType = (searchType != null) ? searchType.toString() : SearchType.ALL.toString();
        String effectiveKeyword = (keyword == null || keyword.isBlank()) ? null : keyword.trim();

        Page<Post> postsPage = postRepository.searchByCondition(category, effectiveSearchType, effectiveKeyword, minLikeCount, pageable);

        Page<PostListResponse> dtoPage = postsPage.map(PostListResponse::of);

        return new PageDto<>(dtoPage, category);
    }

    @Transactional
    public PostLikeResponse likePost(String memberId, Long postId, PostLikeRequest request) {
        // 회원 조회
        Member member = memberService.findMemberByStringId(memberId);

        // 게시글 조회
        Post post = findPostById(postId);

        // 추천 로직 구현
        Optional<PostLike> existPostLike = postLikeRepository.findByMemberAndPost(member, post);

        PostLike postLike;

        if (existPostLike.isEmpty()) {
            // 새로운 추천 생성
            postLikeRepository.save(PostLike.of(member, post, request.getLikeType()));
            // likeCount 증가 또는 감소
            post.changeLikeCount(request.getLikeType() == LikeType.LIKE ? +1 : -1);

        } else if (existPostLike.get().getLikeType() == request.getLikeType()) {
            // 동일한 추천 타입이면 취소 처리
            postLikeRepository.delete(existPostLike.get());
            // likeCount 증가 또는 감소
            post.changeLikeCount(request.getLikeType() == LikeType.LIKE ? -1 : +1);

        } else {
            // 다른 추천 타입이면 변경 처리
            existPostLike.get().changeLikeType(request.getLikeType());
            // likeCount 증가 또는 감소
            post.changeLikeCount(request.getLikeType() == LikeType.LIKE ? +2 : -2);
        }

        // 추천수가 10이상이 되는 순간 popularAt 설정
        post.setPopularAt(POST_POPULAR_LIKE_COUNT);

        return PostLikeResponse.of(post);
    }

    @Transactional
    public void setPostNotice(String memberId, Long postId) {
        Member member = memberService.findMemberByStringId(memberId);

        // 관리자 권한 확인
        if (!member.getRole().equals(Role.ROLE_ADMIN)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        Post post = findPostById(postId);

        boolean notice = post.isNotice();

        post.changeNoticeStatus(!notice);
    }

    // 파일 타입 확인 메서드
    private MediaType resolveMediaType(MultipartFile file) {
        String contentType = file.getContentType();

        if (contentType == null) {
            throw new CustomException(ErrorCode.INVALID_MEDIA_TYPE);
        }

        if (contentType.startsWith("image")) {
            return MediaType.IMAGE;
        }
        if (contentType.startsWith("video")) {
            return MediaType.VIDEO;
        }

        throw new CustomException(ErrorCode.INVALID_MEDIA_TYPE);
    }

    private void savePostMedia(Post post, List<MultipartFile> mediaList) throws IOException {
        int order = 0;

        for (MultipartFile file : mediaList) {
            if (file == null || file.isEmpty()) {
                continue;
            }

            MediaType mediaType = resolveMediaType(file);
            StoredMedia stored = mediaStorage.save(file, mediaType);

            // PostMedia 엔티티 생성 및 저장 로직 추가
            PostMedia postMedia = PostMedia.createPostMedia(
                    post,
                    mediaType,
                    stored.getOriginalName(),
                    stored.getStoredName(),
                    stored.getUrl(),
                    stored.getSize(),
                    order++
            );

            post.addMedia(postMedia);
        }
    }

    public Post findPostById(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
        if (post.getDeletedAt() != null) {
            throw new CustomException(ErrorCode.POST_NOT_FOUND);
        }
        return post;
    }

    // 페이지 처리 및 정렬 조건 생성 메서드
    public Pageable createPageable(int page, int size, PostSortType sort) {
        if (page <= 0) page = 1;

        Sort sortCondition = switch (sort) {
            case POPULAR -> Sort.by(
                    Sort.Order.desc("popularAt"),
                    Sort.Order.desc("createdAt")
            );
            default -> Sort.by(Sort.Order.desc("createdAt"));
        };

        return PageRequest.of(Math.max(0, page - 1), size, sortCondition);
    }

    public boolean existsPostByCategoryId(Long categoryId) {
        return postRepository.existsByCategoryId(categoryId);
    }
}
