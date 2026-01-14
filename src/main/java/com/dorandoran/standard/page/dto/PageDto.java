package com.dorandoran.standard.page.dto;

import com.dorandoran.domain.category.entity.Category;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
public class PageDto<T> {
    private long totalCount;
    private int pageSize;
    private long totalPages;
    private int currentPage;
    private String category;
    private List<T> posts;

    public PageDto(Page<T> page, Category category) {
        this.totalCount = page.getTotalElements();
        this.pageSize = page.getSize();
        this.totalPages = page.getTotalPages();
        this.currentPage = page.getNumber() + 1;
        this.category = (category != null) ? category.getName() : null;
        this.posts = page.getContent();
    }
}