package com.dorandoran.domain.search.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.dorandoran.domain.search.doc.PostDocument;
import com.dorandoran.domain.search.dto.SearchResult;
import com.dorandoran.standard.search.SearchType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostSearchService {

    private final ElasticsearchClient elasticsearchClient;

    public SearchResult searchPostIds(
            SearchType searchType,
            String keyword,
            String category,
            int from,
            int size
    ) throws IOException {

        log.debug("Elasticsearch search called. searchType={}, keyword={}, category={}, from={}, size={}", searchType, keyword, category, from, size);

        // 준비: 필드 목록
        String[] titleFields = {"title", "title.nori", "title.ngram", "title.concat"};
        String[] contentFields = {"content", "content.nori", "content.ngram", "content.concat"};
        String[] authorFields = {"author", "author.nori", "author.ngram", "author.concat"};

        SearchResponse<PostDocument> response;
        try {
            response = elasticsearchClient.search(s -> s
                            .index("posts")
                            .from(from)
                            .size(size)
                            .query(q -> q
                                    .bool(b -> {
                                        if (keyword != null && !keyword.isBlank()) {
                                            String trimmed = keyword.trim();

                                            // 어떤 필드들을 검색할지 searchType에 따라 결정
                                            String[] fieldsToSearch;
                                            if (searchType == null || searchType == SearchType.ALL) {
                                                fieldsToSearch = Stream.concat(
                                                        Stream.concat(Stream.of(titleFields), Stream.of(contentFields)),
                                                        Stream.of(authorFields)
                                                ).toArray(String[]::new);
                                            } else if (searchType == SearchType.TITLE) {
                                                fieldsToSearch = titleFields;
                                            } else if (searchType == SearchType.CONTENT) {
                                                fieldsToSearch = contentFields;
                                            } else if (searchType == SearchType.AUTHOR) {
                                                fieldsToSearch = authorFields;
                                            } else {
                                                fieldsToSearch = Stream.concat(
                                                        Stream.concat(Stream.of(titleFields), Stream.of(contentFields)),
                                                        Stream.of(authorFields)
                                                ).toArray(String[]::new);
                                            }

                                            if (trimmed.length() < 2) {
                                                // 한 글자 검색은 ngram 문제로 와일드카드 폴백 — 대상 필드들에만 적용
                                                b.must(m -> m.bool(sb -> {
                                                    for (String f : fieldsToSearch) {
                                                        sb.should(su -> su.wildcard(w -> w.field(f).value("*" + trimmed + "*")));
                                                    }
                                                    return sb;
                                                }));
                                            } else {
                                                // multiMatch를 대상 필드들로 제한
                                                b.must(m -> m
                                                        .multiMatch(mm -> mm
                                                                .query(keyword)
                                                                .fields(List.of(fieldsToSearch))
                                                        )
                                                );
                                            }
                                        }

                                        if (category != null && !category.isBlank()) {
                                            b.filter(f -> f
                                                    .term(t -> t
                                                            .field("category")
                                                            .value(category)
                                                    )
                                            );
                                        }

                                        return b;
                                    })
                            ),
                    PostDocument.class
            );
        } catch (Exception e) {
            // IOException이나 다른 런타임 예외가 발생할 수 있으므로 상세 로깅 후 빈 결과 반환
            log.error("Elasticsearch search failed. searchType={}, keyword={}, category={}, from={}, size={}. error={}",
                    searchType, keyword, category, from, size, e.toString());
            log.debug("Elasticsearch exception stacktrace", e);

            return new SearchResult(Collections.emptyList(), 0L);
        }

        List<Long> ids = response.hits().hits().stream()
                .map(hit -> {
                    PostDocument src = hit.source();
                    if (src != null && src.getId() != null) return src.getId();
                    String hid = hit.id();
                    if (hid != null && !hid.isBlank()) {
                        try {
                            return Long.valueOf(hid);
                        } catch (NumberFormatException ignored) {
                        }
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .toList();

        long total = 0L;
        if (response.hits().total() != null) {
            total = response.hits().total().value();
        } else {
            total = ids.size();
        }

        log.debug("Elasticsearch search result: ids={}, total={}", ids, total);

        return new SearchResult(ids, total);
    }
}