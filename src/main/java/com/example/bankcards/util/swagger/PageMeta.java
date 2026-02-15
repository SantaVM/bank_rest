package com.example.bankcards.util.swagger;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "PageMeta", description = "Pagination metadata")
public class PageMeta {

    @Schema(example = "10")
    public int size;

    @Schema(example = "0")
    public int number;

    @Schema(example = "2")
    public long totalElements;

    @Schema(example = "1")
    public int totalPages;
}

