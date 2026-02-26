package com.dorandoran.standard.page.dto;

import com.dorandoran.domain.category.entity.Category;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
public class PageWithNoticeDto<T> {
    private final long totalCount;
    private final int pageSize;
    private final long totalPages;
    private final int currentPage;
    private final String category;
    private final List<T> notices;
    private final List<T> posts;

    public PageWithNoticeDto(Page<T> page, Category category, List<T> notices) {
        this.totalCount = page.getTotalElements();
        this.pageSize = page.getSize();
        this.totalPages = page.getTotalPages();
        this.currentPage = page.getNumber() + 1;
        this.category = (category != null) ? category.getName() : null;
        this.notices = notices;
        this.posts = page.getContent();
    }
}