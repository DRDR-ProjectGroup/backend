package com.dorandoran.standard.page.dto;

import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
public class PageCommentDto<T> {
    private long totalCount;
    private int pageSize;
    private long totalPages;
    private int currentPage;
    private List<T> comments;

    public PageCommentDto(Page<T> page) {
        this.totalCount = page.getTotalElements();
        this.pageSize = page.getSize();
        this.totalPages = page.getTotalPages();
        this.currentPage = page.getNumber() + 1;
        this.comments = page.getContent();
    }
}