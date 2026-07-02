package com.library.controller;

import com.library.entity.Fine;
import com.library.service.FineService;
import com.library.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FineControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FineService fineService;

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
    void list_shouldReturnFines() throws Exception {
        Fine fine = new Fine();
        fine.setId(1);
        fine.setAmount(BigDecimal.valueOf(5.00));
        fine.setType("overdue");
        fine.setPaid(false);

        when(fineService.findAll(null, null)).thenReturn(List.of(fine));

        mockMvc.perform(get("/api/fines").header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("overdue"));
    }
}
