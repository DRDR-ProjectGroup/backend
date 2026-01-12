package com.dorandoran.domain.post.service;

import com.dorandoran.domain.category.entity.Category;
import com.dorandoran.domain.category.repository.CategoryRepository;
import com.dorandoran.domain.member.entity.Member;
import com.dorandoran.domain.member.repository.MemberRepository;
import com.dorandoran.domain.post.dto.request.PostCreateRequest;
import com.dorandoran.domain.post.dto.response.PostMediaResponse;
import com.dorandoran.domain.post.dto.response.PostResponse;
import com.dorandoran.domain.post.entity.Post;
import com.dorandoran.domain.post.entity.PostMedia;
import com.dorandoran.domain.post.repository.PostRepository;
import com.dorandoran.domain.post.storage.MediaStorage;
import com.dorandoran.domain.post.storage.StoredMedia;
import com.dorandoran.domain.post.type.MediaType;
import com.dorandoran.global.exception.CustomException;
import com.dorandoran.global.redis.RedisRepository;
import com.dorandoran.global.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final CategoryRepository categoryRepository;
    private final MediaStorage mediaStorage;
    private final RedisRepository redisRepository;

    @Transactional
    public void createPost(String memberId, String categoryName, PostCreateRequest request, List<MultipartFile> files) throws IOException {
        // 회원 조회
        Long parsedId = Long.valueOf(memberId);
        Member member = memberRepository.findById(parsedId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // 카테고리 조회
        Category category = categoryRepository.findByAddress(categoryName)
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));

        // 게시글 생성 로직 구현
        Post post = Post.createPost(member, category, request.getTitle(), request.getContent());

        // 우선 post를 저장해 부모 엔티티가 영속화된 상태로 만듭니다.
        Post saved = postRepository.save(post);

        // 이미지 처리 로직 구현 (필요한 경우) - 부모가 영속 상태이므로, 미디어를 개별 저장합니다.
        if (files != null && !files.isEmpty()) {
            savePostMedia(saved, files);
        }
    }

    @Transactional
    public PostResponse getPostById(Long postId, String viewerIdentifier) {
        // 조회수 증가
        boolean viewed = redisRepository.hasViewedPost(postId, viewerIdentifier);

        if (!viewed) {
            postRepository.incrementViewCount(postId);

            redisRepository.setViewedPost(postId, viewerIdentifier);
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        List<PostMediaResponse> mediaResponses = post.getPostMediaList().stream()
                .map(PostMediaResponse::of)
                .toList();

        return PostResponse.of(post, mediaResponses);
    }

    // 파일 타입 확인 메서드
    private MediaType resolveMediaType(MultipartFile file) {
        if (file.getContentType().startsWith("image")) {
            return MediaType.IMAGE;
        }
        if (file.getContentType().startsWith("video")) {
            return MediaType.VIDEO;
        }
        throw new CustomException(ErrorCode.INVALID_MEDIA_TYPE);
    }

    private void savePostMedia(Post post, List<MultipartFile> mediaList) throws IOException {
        for (int i = 0; i < mediaList.size(); i++) {
            MultipartFile file = mediaList.get(i);
            MediaType mediaType = resolveMediaType(file);

            StoredMedia stored = mediaStorage.save(file, mediaType);

            // PostImage 엔티티 생성 및 저장 로직 추가
            PostMedia postMedia = PostMedia.builder()
                    .post(post)
                    .originalName(stored.getOriginalName())
                    .storedName(stored.getStoredName())
                    .url(stored.getUrl())
                    .size(stored.getSize())
                    .mediaType(mediaType)
                    .sortOrder(i)
                    .build();

            post.addMedia(postMedia);
        }
    }
}
