package com.dorandoran.domain.search.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.dorandoran.domain.search.doc.PostDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class PostIndexService {

    private final ElasticsearchClient elasticsearchClient;

    public void index(PostDocument postDocument) throws IOException {
        elasticsearchClient.index(i -> i
                .index("posts")
                .id(postDocument.getId().toString())
                .document(postDocument)
        );
    }

    public void delete(Long id) throws IOException {
        elasticsearchClient.delete(d -> d
                .index("posts")
                .id(id.toString())
        );
    }
}
