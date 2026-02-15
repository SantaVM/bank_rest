package com.example.bankcards.util.swagger;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(name = "PageResponse")
public class PageResponse<T> {

    @Schema(description = "Page content")
    public List<T> content;

    @Schema(description = "Pagination info")
    public PageMeta page;
}

