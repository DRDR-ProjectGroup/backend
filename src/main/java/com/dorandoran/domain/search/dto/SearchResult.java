package com.dorandoran.domain.search.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class SearchResult {
    private List<Long> ids;
    private long totalCount;
}
