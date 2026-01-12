package com.dorandoran.factory;

import com.dorandoran.domain.category.entity.Category;
import com.dorandoran.domain.member.entity.Member;
import com.dorandoran.domain.post.entity.Post;
import com.dorandoran.domain.post.entity.PostMedia;
import com.dorandoran.domain.post.repository.PostMediaRepository;
import com.dorandoran.domain.post.repository.PostRepository;
import com.dorandoran.domain.post.type.MediaType;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public final class PostFactory {

    public static final String TITLE_PREFIX = "title";
    public static final String CONTENT_PREFIX = "content";

    private final EntityManager em;
    private final PostRepository postRepository;
    private final PostMediaRepository postMediaRepository;

    public List<Post> saveAndCreatePost(Member member, Category category, final int count) {
        if (count == 0) return List.of();

        List<Post> savedPostList = new ArrayList<>(count);

        for (int i = 1; i <= count; i++) {
            String title = String.format("%s%d", TITLE_PREFIX, i);
            String content = String.format("%s%d", CONTENT_PREFIX, i);

            Post newPost = Post.createPost(member, category, title, content);

            PostMedia newPostMedia = PostMedia.createPostMedia(
                    newPost,
                    MediaType.IMAGE,
                    "originalName" + i,
                    "storedName" + i,
                    "url" + i,
                    1024L,
                    1
            );
            newPost.addMedia(newPostMedia);
            Post savedPost = postRepository.save(newPost);
            savedPostList.add(savedPost);
        }

        flushAndClear();

        return savedPostList;
    }

    private void flushAndClear() {
        em.flush();
        em.clear();
    }
}
