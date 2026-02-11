package com.dorandoran.standard.page.dto;

import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
public class PageCommentDto<T> {
    private final long totalCount;
    private final int pageSize;
    private final long totalPages;
    private final int currentPage;
    private final List<T> comments;

    public PageCommentDto(Page<T> page) {
        this.totalCount = page.getTotalElements();
        this.pageSize = page.getSize();
        this.totalPages = page.getTotalPages();
        this.currentPage = page.getNumber() + 1;
        this.comments = page.getContent();
    }
}