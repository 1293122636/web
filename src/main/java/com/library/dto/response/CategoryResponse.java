package com.library.dto.response;

import lombok.Data;

@Data
public class CategoryResponse {
    private Integer id;
    private String name;
    private String desc;
    private Integer booksCount;

    public CategoryResponse(Integer id, String name, String desc, Integer booksCount) {
        this.id = id;
        this.name = name;
        this.desc = desc;
        this.booksCount = booksCount;
    }

    public CategoryResponse() {}
}
