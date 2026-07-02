package com.library.dto.response;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class FacetsDTO {
    private Map<String, List<Map<String, Object>>> facets;
}
