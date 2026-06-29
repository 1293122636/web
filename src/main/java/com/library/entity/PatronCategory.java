package com.library.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PatronCategory {
    private Integer id;
    private String name;
    private LocalDateTime createdAt;
}
