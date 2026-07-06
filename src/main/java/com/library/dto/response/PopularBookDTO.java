package com.library.dto.response;

import lombok.Data;
import java.util.Map;

@Data
public class PopularBookDTO {
    private Integer id;
    private String title;
    private String author;
    private String isbn;
    private Integer categoryId;
    private CategoryResponse category;
    private Map<String, Object> _count;
}
