package com.dorandoran.domain.post.service;

import com.dorandoran.SpringBootTestSupporter;
import com.dorandoran.domain.category.entity.Category;
import com.dorandoran.domain.category.entity.CategoryGroup;
import com.dorandoran.domain.member.entity.Member;
import com.dorandoran.domain.post.dto.request.PostCreateRequest;
import com.dorandoran.domain.post.dto.request.PostLikeRequest;
import com.dorandoran.domain.post.entity.PostLike;
import com.dorandoran.domain.post.type.LikeType;
import com.dorandoran.global.response.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
class PostServiceTest extends SpringBootTestSupporter {

    private Member member;
    private Member secondMember;
    private Category category;

    @BeforeEach
    void setUp() {
        List<Member> memberList = memberFactory.saveAndCreateMember(3);
        member = memberList.get(0);
        secondMember = memberList.get(1);
        List<CategoryGroup> categoryGroups = categoryGroupFactory.saveAndCreateDefaultCategoryGroup();
        category = categoryFactory.saveAndCreateCategory("게임", "lol");
    }

    @DisplayName("게시글 생성 실패 - 존재하지 않는 회원")
    @Test
    void createPost_Fail_NonExistentMember() {
        // given
        String memberId = "9999";
        String categoryName = "lol";
        PostCreateRequest request = new PostCreateRequest("게시글 제목", "게시글 내용");
        List<MultipartFile> files = List.of(
                new MockMultipartFile(
                        "files",
                        "image1.png",
                        "image/png",
                        "dummy image content".getBytes()
                ),
                new MockMultipartFile(
                        "files",
                        "image2.jpg",
                        "image/jpeg",
                        "dummy image content".getBytes()
                )
        );

        // when // then
        assertThatThrownBy(() -> postService.createPost(memberId, categoryName, request, files))
                .isInstanceOf(Exception.class)
                .extracting("code")
                .isEqualTo(ErrorCode.MEMBER_NOT_FOUND);
    }

    @DisplayName("게시글 생성 실패 - 존재하지 않는 카테고리")
    @Test
    void createPost_Fail_NonExistentCategory() {
        // given
        String memberId = member.getId().toString();
        String categoryName = "nonexistent-category";
        PostCreateRequest request = new PostCreateRequest("게시글 제목", "게시글 내용");
        List<MultipartFile> files = List.of(
                new MockMultipartFile(
                        "files",
                        "image1.png",
                        "image/png",
                        "dummy image content".getBytes()
                ),
                new MockMultipartFile(
                        "files",
                        "image2.jpg",
                        "image/jpeg",
                        "dummy image content".getBytes()
                )
        );

        // when // then
        assertThatThrownBy(() -> postService.createPost(memberId, categoryName, request, files))
                .isInstanceOf(Exception.class)
                .extracting("code")
                .isEqualTo(ErrorCode.CATEGORY_NOT_FOUND);
    }

    @DisplayName("미디어 저장 실패 - 잘못된 파일 형식")
    @Test
    void mediaType_Fail() {
        // given
        MultipartFile invalidFile = new MockMultipartFile(
                "files",
                "document.pdf",
                "application/pdf",
                "dummy pdf content".getBytes()
        );

        // when // then
        assertThatThrownBy(() -> {
            postService.createPost(
                    member.getId().toString(),
                    category.getAddress(),
                    new PostCreateRequest("게시글 제목", "게시글 내용"),
                    List.of(invalidFile)
            );
        })
                .isInstanceOf(Exception.class)
                .extracting("code")
                .isEqualTo(ErrorCode.INVALID_MEDIA_TYPE);
    }

    @DisplayName("게시글 조회 실패 - 존재하지 않는 게시글")
    @Test
    void getPost_Fail_NonExistentPost() {
        // given
        Long nonExistentPostId = 9999L;
        String memberId = member.getId().toString();

        // when // then
        assertThatThrownBy(() -> postService.getPostById(nonExistentPostId, memberId))
                .isInstanceOf(Exception.class)
                .extracting("code")
                .isEqualTo(ErrorCode.POST_NOT_FOUND);
    }

    @DisplayName("게시글 조회 - 조회수 증가 (30분에 1번만 가능)")
    @Test
    void getPost_Success_IncreaseViewCount() throws Exception {
        // given
        Long postId = postFactory.saveAndCreatePost(member, category, 1).getFirst().getId();
        String memberId = member.getId().toString();

        // when
        postService.getPostById(postId, memberId);
        postService.getPostById(postId, memberId); // 두 번째 조회는 조회수 증가 안됨

        // then
        int viewCount = postRepository.findById(postId).get().getViewCount();
        assert (viewCount == 1);
    }

    @DisplayName("게시글 작성 - 삭제된 게시글이면 조회 안됨")
    @Test
    void getPost_Fail_DeletedPost() {
        // given
        Long postId = postFactory.saveAndCreatePost(member, category, 1).getFirst().getId();
        postService.deletePost(postId, member.getId().toString());
        String memberId = member.getId().toString();

        // when // then
        assertThatThrownBy(() -> postService.getPostById(postId, memberId))
                .isInstanceOf(Exception.class)
                .extracting("code")
                .isEqualTo(ErrorCode.POST_NOT_FOUND);
    }

    @DisplayName("게시글 수정 실패 - 존재하지 않는 회원")
    @Test
    void modifyPost_Fail_NonExistentMember() {
        // given
        Long postId = postFactory.saveAndCreatePost(member, category, 1).getFirst().getId();
        String memberId = "9999";
        PostCreateRequest request = new PostCreateRequest("수정된 제목", "수정된 내용");
        List<MultipartFile> files = List.of(
                new MockMultipartFile(
                        "files",
                        "image1.png",
                        "image/png",
                        "dummy image content".getBytes()
                )
        );

        // when // then
        assertThatThrownBy(() -> postService.modifyPost(memberId, postId, request, files))
                .isInstanceOf(Exception.class)
                .extracting("code")
                .isEqualTo(ErrorCode.MEMBER_NOT_FOUND);
    }

    @DisplayName("게시글 수정 실패 - 존재하지 않는 게시글")
    @Test
    void modifyPost_Fail_NonExistentPost() {
        // given
        Long nonExistentPostId = 9999L;
        String memberId = member.getId().toString();
        PostCreateRequest request = new PostCreateRequest("수정된 제목", "수정된 내용");
        List<MultipartFile> files = List.of(
                new MockMultipartFile(
                        "files",
                        "image1.png",
                        "image/png",
                        "dummy image content".getBytes()
                )
        );

        // when // then
        assertThatThrownBy(() -> postService.modifyPost(memberId, nonExistentPostId, request, files))
                .isInstanceOf(Exception.class)
                .extracting("code")
                .isEqualTo(ErrorCode.POST_NOT_FOUND);
    }

    @DisplayName("게시글 수정 실패 - 작성자 불일치")
    @Test
    void modifyPost_Fail_AuthorMismatch() {
        // given
        Long postId = postFactory.saveAndCreatePost(member, category, 1).getFirst().getId();
        String secondMemberId = secondMember.getId().toString();
        PostCreateRequest request = new PostCreateRequest("수정된 제목", "수정된 내용");
        List<MultipartFile> files = List.of(
                new MockMultipartFile(
                        "files",
                        "image1.png",
                        "image/png",
                        "dummy image content".getBytes()
                )
        );

        // when // then
        assertThatThrownBy(() -> postService.modifyPost(secondMemberId, postId, request, files))
                .isInstanceOf(Exception.class)
                .extracting("code")
                .isEqualTo(ErrorCode.UNAUTHORIZED_POST_MODIFICATION);
    }

    @DisplayName("게시글 수정 - 삭제된 게시글이면 수정 안됨")
    @Test
    void modifyPost_Fail_DeletedPost() {
        // given
        Long postId = postFactory.saveAndCreatePost(member, category, 1).getFirst().getId();
        postService.deletePost(postId, member.getId().toString());
        String memberId = member.getId().toString();
        PostCreateRequest request = new PostCreateRequest("수정된 제목", "수정된 내용");
        List<MultipartFile> files = List.of(
                new MockMultipartFile(
                        "files",
                        "image1.png",
                        "image/png",
                        "dummy image content".getBytes()
                )
        );

        // when // then
        assertThatThrownBy(() -> postService.modifyPost(memberId, postId, request, files))
                .isInstanceOf(Exception.class)
                .extracting("code")
                .isEqualTo(ErrorCode.POST_NOT_FOUND);
    }

    @DisplayName("파일 타입 확인 - contentType이 null인 경우")
    @Test
    void validateFileType_Fail_NullContentType() {
        // given
        MultipartFile invalidFile = new MockMultipartFile(
                "files",
                "unknownfile",
                null,
                "dummy content".getBytes()
        );

        // when // then
        assertThatThrownBy(() -> {
            postService.createPost(
                    member.getId().toString(),
                    category.getAddress(),
                    new PostCreateRequest("게시글 제목", "게시글 내용"),
                    List.of(invalidFile)
            );
        })
                .isInstanceOf(Exception.class)
                .extracting("code")
                .isEqualTo(ErrorCode.INVALID_MEDIA_TYPE);
    }

    @DisplayName("게시글 삭제 실패 - 존재하지 않는 회원")
    @Test
    void deletePost_Fail_NonExistentMember() {
        // given
        Long postId = postFactory.saveAndCreatePost(member, category, 1).getFirst().getId();
        String memberId = "9999";

        // when // then
        assertThatThrownBy(() -> postService.deletePost(postId, memberId))
                .isInstanceOf(Exception.class)
                .extracting("code")
                .isEqualTo(ErrorCode.MEMBER_NOT_FOUND);
    }

    @DisplayName("게시글 삭제 실패 - 존재하지 않는 게시글")
    @Test
    void deletePost_Fail_NonExistentPost() {
        // given
        Long nonExistentPostId = 9999L;
        String memberId = member.getId().toString();

        // when // then
        assertThatThrownBy(() -> postService.deletePost(nonExistentPostId, memberId))
                .isInstanceOf(Exception.class)
                .extracting("code")
                .isEqualTo(ErrorCode.POST_NOT_FOUND);
    }

    @DisplayName("게시글 삭제 실패 - 작성자 불일치")
    @Test
    void deletePost_Fail_AuthorMismatch() {
        // given
        Long postId = postFactory.saveAndCreatePost(member, category, 1).getFirst().getId();
        String secondMemberId = secondMember.getId().toString();

        // when // then
        assertThatThrownBy(() -> postService.deletePost(postId, secondMemberId))
                .isInstanceOf(Exception.class)
                .extracting("code")
                .isEqualTo(ErrorCode.UNAUTHORIZED_POST_MODIFICATION);
    }

    @DisplayName("게시글 삭제 - 삭제된 게시글이면 삭제 안됨")
    @Test
    void deletePost_Fail_DeletedPost() {
        // given
        Long postId = postFactory.saveAndCreatePost(member, category, 1).getFirst().getId();
        postService.deletePost(postId, member.getId().toString());
        String memberId = member.getId().toString();

        // when // then
        assertThatThrownBy(() -> postService.deletePost(postId, memberId))
                .isInstanceOf(Exception.class)
                .extracting("code")
                .isEqualTo(ErrorCode.POST_NOT_FOUND);
    }

    @DisplayName("게시글 목록 조회 - 존재하지 않는 카테고리")
    @Test
    void getPostsByCategory_Fail_NonExistentCategory() {
        // given
        String nonExistentCategoryName = "nonexistent-category";

        // when // then
        assertThatThrownBy(() -> postService.getPostsByCategory(nonExistentCategoryName, null, null, 1, 20, null))
                .isInstanceOf(Exception.class)
                .extracting("code")
                .isEqualTo(ErrorCode.CATEGORY_NOT_FOUND);
    }

    @DisplayName("게시글 추천 - 처음 추천")
    @Test
    void likePost_Success_FirstLike() throws Exception {
        // given
        Long postId = postFactory.saveAndCreatePost(member, category, 1).getFirst().getId();

        // when
        postService.likePost(member.getId().toString(), postId, new PostLikeRequest(LikeType.LIKE));

        // then
        int likeCount = postRepository.findById(postId).get().getLikeCount();
        assertThat(likeCount).isEqualTo(1);
        // DB에 추천 레코드가 저장되었는지 확인
        assertThat(postLikeRepository.findByMemberAndPost(member, postRepository.findById(postId).get())).isPresent();
    }

    @DisplayName("게시글 추천 - 추천 취소")
    @Test
    void likePost_Success_CancelLike() throws Exception {
        // given
        Long postId = postFactory.saveAndCreatePost(member, category, 1).getFirst().getId();
        // 먼저 추천
        postService.likePost(member.getId().toString(), postId, new PostLikeRequest(LikeType.LIKE));

        // when: 동일 타입으로 다시 요청 -> 취소
        postService.likePost(member.getId().toString(), postId, new PostLikeRequest(LikeType.LIKE));

        // then
        int likeCount = postRepository.findById(postId).get().getLikeCount();
        assertThat(likeCount).isEqualTo(0);
        assertThat(postLikeRepository.findByMemberAndPost(member, postRepository.findById(postId).get())).isEmpty();
    }

    @DisplayName("게시글 추천 - 추천에서 비추천으로 변경")
    @Test
    void likePost_Success_ChangeLikeToDislike() throws Exception {
        // given
        Long postId = postFactory.saveAndCreatePost(member, category, 1).getFirst().getId();
        // 먼저 추천
        postService.likePost(member.getId().toString(), postId, new PostLikeRequest(LikeType.LIKE));

        // when: 다른 타입으로 변경
        postService.likePost(member.getId().toString(), postId, new PostLikeRequest(LikeType.DISLIKE));

        // then
        int likeCount = postRepository.findById(postId).get().getLikeCount();
        // 기존 1에서 -2 변화 => -1
        assertThat(likeCount).isEqualTo(-1);
        // DB에 변경된 likeType이 반영되었는지 확인
        PostLike like = postLikeRepository.findByMemberAndPost(member, postRepository.findById(postId).get()).orElseThrow();
        assertThat(like.getLikeType()).isEqualTo(LikeType.DISLIKE);
    }
}