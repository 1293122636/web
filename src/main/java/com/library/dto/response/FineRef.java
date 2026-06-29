package com.library.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FineRef {
    private Integer id;
    private Double amount;
    private String type;
    private Boolean paid;
}
