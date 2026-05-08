package com.project.ArtRari.common;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter @Setter
public class PageResponse<T>{
    private List<T> items;
    private int totalPages;
    private long totalElements;
    private int currentPage;
    private boolean hasNext;

    public PageResponse(Page<T> page){
        this.items = page.getContent();
        this.totalPages = page.getTotalPages();
        this.totalElements = page.getTotalElements();
        this.currentPage = page.getNumber();
        this.hasNext = page.hasNext();
    }
}

