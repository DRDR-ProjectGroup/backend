package com.dorandoran.domain.search.service;

import com.dorandoran.domain.post.entity.Post;
import com.dorandoran.domain.post.repository.PostRepository;
import com.dorandoran.domain.search.doc.PostDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostBulkIndexService {

    private final PostRepository postRepository;
    private final PostIndexService postIndexService;

    public void bulkIndex() throws IOException {
        List<Post> posts = postRepository.findAll();

        for (Post post : posts) {
            PostDocument doc = PostDocument.createDoc(post);

            postIndexService.index(doc);
        }
    }
}
