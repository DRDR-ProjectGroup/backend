package com.dorandoran.standard.page.dto;

import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
public class PageDto<T> {
    private long totalCount;
    private int pageSize;
    private long totalPages;
    private int currentPage;
    private List<T> posts;

    public PageDto(Page<T> page) {
        this.totalCount = page.getTotalElements();
        this.pageSize = page.getSize();
        this.totalPages = page.getTotalPages();
        this.currentPage = page.getNumber() + 1;
        this.posts = page.getContent();
    }
}