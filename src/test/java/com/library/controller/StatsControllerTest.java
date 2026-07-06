package com.library.controller;

import com.library.dto.response.PopularBookDTO;
import com.library.service.StatsService;
import com.library.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class StatsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StatsService statsService;

    @MockBean
    private JwtUtil jwtUtil;

    private final String adminToken = "Bearer test-admin-token";

    @BeforeEach
    void setUp() {
        when(jwtUtil.validateToken(anyString())).thenReturn(true);
        when(jwtUtil.getUserIdFromToken(anyString())).thenReturn(1);
        when(jwtUtil.getRoleFromToken(anyString())).thenReturn("admin");
    }

    @Test
    void overview_shouldReturnStats() throws Exception {
        when(statsService.getOverview()).thenReturn(null);

        mockMvc.perform(get("/api/stats").header("Authorization", adminToken))
                .andExpect(status().isOk());
    }

    @Test
    void popular_shouldReturnList() throws Exception {
        PopularBookDTO dto = new PopularBookDTO();
        dto.setId(1);
        dto.setTitle("Test Book");
        dto.setAuthor("Author");
        dto.setIsbn("1234567890");
        dto.set_count(Map.of("borrowRecords", 5));

        when(statsService.getPopular()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/stats/popular").header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Test Book"))
                .andExpect(jsonPath("$[0]._count.borrowRecords").value(5));
    }
}
