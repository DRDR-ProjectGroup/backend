package com.dorandoran.domain.post.service;

import com.dorandoran.domain.category.entity.Category;
import com.dorandoran.domain.category.repository.CategoryRepository;
import com.dorandoran.domain.member.entity.Member;
import com.dorandoran.domain.member.service.MemberService;
import com.dorandoran.domain.member.type.Role;
import com.dorandoran.domain.post.dto.request.PostCreateRequest;
import com.dorandoran.domain.post.dto.request.PostLikeRequest;
import com.dorandoran.domain.post.dto.request.PostModifyRequest;
import com.dorandoran.domain.post.dto.response.*;
import com.dorandoran.domain.post.entity.Post;
import com.dorandoran.domain.post.entity.PostLike;
import com.dorandoran.domain.post.entity.PostMedia;
import com.dorandoran.domain.post.generator.MediaUrlResolver;
import com.dorandoran.domain.post.repository.PostLikeRepository;
import com.dorandoran.domain.post.repository.PostMediaRepository;
import com.dorandoran.domain.post.repository.PostRepository;
import com.dorandoran.domain.post.storage.MediaStorage;
import com.dorandoran.domain.post.storage.StoredMedia;
import com.dorandoran.domain.post.type.LikeType;
import com.dorandoran.domain.post.type.MediaType;
import com.dorandoran.domain.post.type.PostSortType;
import com.dorandoran.domain.search.dto.SearchResult;
import com.dorandoran.domain.search.service.PostIndexService;
import com.dorandoran.domain.search.service.PostSearchService;
import com.dorandoran.global.exception.CustomException;
import com.dorandoran.global.redis.RedisRepository;
import com.dorandoran.global.response.ErrorCode;
import com.dorandoran.standard.page.dto.PageWithNoticeDto;
import com.dorandoran.standard.search.SearchType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {

    private final PostRepository postRepository;
    private final PostMediaRepository postMediaRepository;
    private final MemberService memberService;
    private final CategoryRepository categoryRepository;
    private final MediaStorage mediaStorage;
    private final RedisRepository redisRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostIndexService postIndexService;
    private final PostSearchService postSearchService;
    private final MediaUrlResolver mediaUrlResolver;

    @Value("${search.elastic.enabled}")
    private boolean elasticEnabled;

    @Value("${post.popular.like-count-threshold}")
    private int postPopularLikeCount;

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

//        // 검색을 위한 색인 작업 (미디어 저장 후, 트랜잭션 커밋 후 실행)
//        PostDocument doc = PostDocument.createDoc(saved);
//        if (TransactionSynchronizationManager.isSynchronizationActive()) {
//            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
//                @Override
//                public void afterCommit() {
//                    try {
//                        postIndexService.index(doc);
//                    } catch (IOException e) {
//                        log.error("Failed to index post after commit, id={}", saved.getId(), e);
//                    }
//                }
//            });
//        } else {
//            postIndexService.index(doc);
//        }


        List<PostMediaResponse> mediaResponses = mapMediaResponses(saved.getPostMediaList());

        return PostResponse.of(saved, mediaResponses);
    }

    @Transactional
    public PostResponseWithLikeType getPostById(Long postId, String memberId, String guestToken) {
        // 조회수 처리용 식별자 결정
        String viewerIdentifier = (memberId != null) ? memberId : guestToken;

        // 조회수 증가
        boolean viewed = redisRepository.hasViewedPost(postId, viewerIdentifier);

        if (!viewed) {
            postRepository.incrementViewCount(postId);

            redisRepository.setViewedPost(postId, viewerIdentifier);
        }

        Post post = findPostById(postId);

        List<PostMediaResponse> mediaResponses = mapMediaResponses(post.getPostMediaList());

        return PostResponseWithLikeType.of(post, mediaResponses);
    }

    @Transactional
    public PostResponse modifyPost(String memberId, Long postId, PostModifyRequest dto, List<MultipartFile> files) throws IOException {
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

        // olderMediaIdsAndOrders로 전달받은 id와 order로 기존 미디어의 순서를 업데이트
        Map<Long, Integer> olderMediaIdsAndOrders = dto.getOldMediaIdsAndOrders();
        if (olderMediaIdsAndOrders != null) {
            for (Map.Entry<Long, Integer> entry : olderMediaIdsAndOrders.entrySet()) {
                Long mediaId = entry.getKey();
                Integer order = entry.getValue();

                PostMedia postMedia = postMediaRepository.findByIdAndPostId(mediaId, postId)
                        .orElseThrow(() -> new CustomException(ErrorCode.POST_MEDIA_NOT_FOUND));

                postMedia.updateOrder(order);
            }
        }

        // deletedMediaIds에 포함된 id는 삭제 처리 (파일 스토리지에서도 삭제 필요)
        List<Long> deletedMediaIds = dto.getDeletedMediaIds();
        if (deletedMediaIds != null && !deletedMediaIds.isEmpty()) {
            List<String> objectKeys = new ArrayList<>();

            for (Long mediaId : deletedMediaIds) {
                PostMedia postMedia = postMediaRepository.findByIdAndPostId(mediaId, postId)
                        .orElseThrow(() -> new CustomException(ErrorCode.POST_MEDIA_NOT_FOUND));

                // 파일 스토리지에서 삭제
                if (postMedia.getObjectKey() != null && !postMedia.getObjectKey().isBlank()) {
                    objectKeys.add(postMedia.getObjectKey());
                }

                // DB에서 삭제
                postMediaRepository.delete(postMedia);

                // 게시글의 미디어 리스트에서도 제거 (영속성 컨텍스트에서 관리되는 객체이므로, 직접 리스트에서 제거)
                post.getPostMediaList().remove(postMedia);
            }

//            mediaStorage.delete(objectKeys);
        }

        // 이미지 처리 로직 구현 (파일이 주어지면 기존 미디어를 교체)
        List<Integer> newMediaOrders = dto.getNewMediaOrders();
        if (files != null && !files.isEmpty() && newMediaOrders != null && !newMediaOrders.isEmpty()) {
            savePostMedia(post, files, newMediaOrders);
        }

//        // 수정 후 색인 갱신을 트랜잭션 커밋 후 실행 (DB 정합성을 위해 DB에 반영된 후 색인 작업 수행)
//        PostDocument doc = PostDocument.createDoc(post);
//        if (TransactionSynchronizationManager.isSynchronizationActive()) {
//            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
//                @Override
//                public void afterCommit() {
//                    try {
//                        postIndexService.index(doc);
//                    } catch (IOException e) {
//                        log.error("Failed to index post after commit, id={}", post.getId(), e);
//                    }
//                }
//            });
//        } else {
//            // 트랜잭션이 없으면 즉시 실행
//            postIndexService.index(doc);
//        }

        List<PostMediaResponse> mediaResponses = mapMediaResponses(post.getPostMediaList());

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

//        // 게시글 hard delete 방법
//        List<String> objectKeys = post.getPostMediaList().stream()
//                .map(PostMedia::getObjectKey)
//                .filter(objectKey -> objectKey != null && !objectKey.isBlank())
//                .toList();
//
//        postRepository.delete(post);
//        mediaStorage.delete(objectKeys);

        post.setDeletedAt();

//        // soft-delete 후 ES 색인에서 해당 문서 삭제를 트랜잭션 커밋 후 실행
//        if (TransactionSynchronizationManager.isSynchronizationActive()) {
//            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
//                @Override
//                public void afterCommit() {
//                    try {
//                        postIndexService.delete(postId);
//                    } catch (IOException e) {
//                        log.error("Failed to delete post index after commit for id={}", postId, e);
//                    }
//                }
//            });
//        } else {
//            try {
//                postIndexService.delete(postId);
//            } catch (IOException e) {
//                log.error("Failed to delete post index for id={}", postId, e);
//            }
//        }
    }

    @Transactional(readOnly = true)
    public PageWithNoticeDto<PostListResponse> getPostsByCategory(String categoryName, SearchType searchType, String keyword, int page, int size, PostSortType sort) {
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

    private PageWithNoticeDto<PostListResponse> searchByElasticSearch(SearchType searchType, String keyword, int page, int size, PostSortType sort, Category category) {
        List<PostListResponse> notices = List.of();
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

        if (ids == null || ids.isEmpty()) {
            Page<PostListResponse> emptyPage = new PageImpl<>(
                    List.<PostListResponse>of(),
                    PageRequest.of(Math.max(0, page - 1), size),
                    0L
            );
            return new PageWithNoticeDto<>(emptyPage, category, notices);
        }

        // DB에서 해당 id들 조회
        List<Post> posts;
        if (sort == PostSortType.POPULAR) {
            posts = postRepository.findPopularPostsByIds(ids, postPopularLikeCount);
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

        return new PageWithNoticeDto<>(pageImpl, category, notices);
    }

    private PageWithNoticeDto<PostListResponse> searchByDatabase(SearchType searchType, String keyword, int page, int size, PostSortType sort, Category category) {
        Pageable pageable = createPageable(page, size, sort);

        Integer minLikeCount = (sort == PostSortType.POPULAR) ? postPopularLikeCount : null;
        String effectiveSearchType = (searchType != null) ? searchType.toString() : SearchType.ALL.toString();
        String effectiveKeyword = (keyword == null || keyword.isBlank()) ? null : keyword.trim();

        Page<Post> postsPage = postRepository.searchByCondition(category, effectiveSearchType, effectiveKeyword, minLikeCount, pageable);

        Page<PostListResponse> dtoPage = postsPage.map(PostListResponse::of);

        List<PostListResponse> notices = List.of();
        if (page == 1 && effectiveKeyword == null) {
            notices = postRepository.findByCategoryAndIsNoticeTrue(category).stream()
                    .map(PostListResponse::of)
                    .toList();

            if (category == null) {
                notices = postRepository.findByIsNoticeTrue().stream()
                        .map(PostListResponse::of)
                        .toList();
            }
        }

        return new PageWithNoticeDto<>(dtoPage, category, notices);
    }

    @Transactional
    public PostLikeResponse likePost(String memberId, Long postId, PostLikeRequest request) {
        // 회원 조회
        Member member = memberService.findMemberByStringId(memberId);

        // 게시글 조회
        Post post = findPostById(postId);

        // 추천 로직 구현
        Optional<PostLike> existPostLike = postLikeRepository.findByMemberAndPost(member, post);

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
        post.setPopularAt(postPopularLikeCount);

        PostLike postLike = postLikeRepository.findByMemberAndPost(member, post)
                .orElse(null);

        return PostLikeResponse.of(post, postLike);
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

    @Transactional(readOnly = true)
    public PostLikeResponse getPostLikeCount(Long postId, String memberId) {
        Post post = findPostById(postId);

        PostLike postLike = null;

        if (memberId != null) {
            Member member = memberService.findMemberByStringId(memberId);
            postLike = postLikeRepository.findByMemberAndPost(member, post)
                    .orElse(null);
        }

        return PostLikeResponse.of(post, postLike);
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
        final long MAX_FILE_SIZE = 20L * 1024 * 1024; // 20 MB
        final int MAX_FILES_PER_POST = 5;

        int existingCount = post.getPostMediaList() != null ? post.getPostMediaList().size() : 0;

        int incomingCount = 0;
        for (MultipartFile f : mediaList) {
            if (f != null && !f.isEmpty()) incomingCount++;
        }

        if (existingCount + incomingCount > MAX_FILES_PER_POST) {
            throw new CustomException(ErrorCode.MEDIA_COUNT_EXCEEDED);
        }

        int order = existingCount; // 기존 미디어 다음 순서부터 시작

        for (MultipartFile file : mediaList) {
            if (file == null || file.isEmpty()) {
                continue;
            }

            if (file.getSize() > MAX_FILE_SIZE) {
                throw new CustomException(ErrorCode.MEDIA_FILE_TOO_LARGE);
            }

            MediaType mediaType = resolveMediaType(file);
            StoredMedia stored = mediaStorage.save(file, mediaType);

            // PostMedia 엔티티 생성 및 저장 로직 추가
            PostMedia postMedia = PostMedia.createPostMedia(
                    post,
                    mediaType,
                    stored.getOriginalName(),
                    stored.getStoredName(),
                    stored.getObjectKey(),
                    stored.getSize(),
                    order++
            );

            post.addMedia(postMedia);

            postMediaRepository.save(postMedia);
        }
    }

    private void savePostMedia(Post post, List<MultipartFile> mediaList, List<Integer> newMediaOrders) throws IOException {
        final long MAX_FILE_SIZE = 20L * 1024 * 1024;
        final int remaining = 5 - post.getPostMediaList().size();

        int incomingCount = 0;
        for (MultipartFile f : mediaList) {
            if (f != null && !f.isEmpty()) incomingCount++;
        }

        if (incomingCount > remaining) {
            throw new CustomException(ErrorCode.MEDIA_COUNT_EXCEEDED);
        }

        for (int i = 0; i < mediaList.size(); i++) {
            MultipartFile file = mediaList.get(i);

            if (file == null || file.isEmpty()) {
                continue;
            }

            if (file.getSize() > MAX_FILE_SIZE) {
                throw new CustomException(ErrorCode.MEDIA_FILE_TOO_LARGE);
            }

            MediaType mediaType = resolveMediaType(file);
            StoredMedia stored = mediaStorage.save(file, mediaType);

            // PostMedia 엔티티 생성 및 저장 로직 추가
            PostMedia postMedia = PostMedia.createPostMedia(
                    post,
                    mediaType,
                    stored.getOriginalName(),
                    stored.getStoredName(),
                    stored.getObjectKey(),
                    stored.getSize(),
                    newMediaOrders.get(i)
            );

            post.addMedia(postMedia);

            postMediaRepository.save(postMedia);
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

    public boolean existsNotDeletedPostByCategoryId(Long categoryId) {
        return postRepository.existsByCategoryIdAndDeletedAtIsNull(categoryId);
    }

    private List<PostMediaResponse> mapMediaResponses(List<PostMedia> mediaList) {
        return mediaList.stream()
                .sorted(Comparator.comparingInt(PostMedia::getSortOrder))
                .map(media -> PostMediaResponse.of(media, mediaUrlResolver.resolve(media)))
                .toList();
    }

    @Transactional
    public void deleteExpiredPost() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(30);

        // 삭제된지 threshold(30일) 지난 게시글과 그 게시글의 미디어를 hard delete 처리
        List<Post> expiredPosts = postRepository.findAllByDeletedAtBefore(threshold);

        expiredPosts.forEach(post -> {
            List<String> objectKeys = post.getPostMediaList().stream()
                    .map(PostMedia::getObjectKey)
                    .filter(objectKey -> objectKey != null && !objectKey.isBlank())
                    .toList();

            postRepository.delete(post);
            postMediaRepository.deleteAll(post.getPostMediaList());
            try {
                mediaStorage.delete(objectKeys);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        // 삭제된지 threshold(30일) 지난 게시글과 연관된 미디어 삭제
    }
}
