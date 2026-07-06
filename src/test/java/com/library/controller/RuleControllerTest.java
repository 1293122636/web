package com.library.controller;

import com.library.entity.CirculationRule;
import com.library.entity.ItemType;
import com.library.entity.PatronCategory;
import com.library.mapper.CirculationRuleMapper;
import com.library.mapper.ItemTypeMapper;
import com.library.mapper.PatronCategoryMapper;
import com.library.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RuleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CirculationRuleMapper ruleMapper;
    @MockBean
    private PatronCategoryMapper patronCategoryMapper;
    @MockBean
    private ItemTypeMapper itemTypeMapper;

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
    void list_shouldReturnRules() throws Exception {
        CirculationRule r = new CirculationRule();
        r.setId(1);
        r.setMaxBorrows(5);
        r.setLoanDays(30);
        when(ruleMapper.findAll()).thenReturn(List.of(r));

        mockMvc.perform(get("/api/rules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].maxBorrows").value(5));
    }

    @Test
    void getPatronCategories_shouldReturnList() throws Exception {
        PatronCategory p = new PatronCategory();
        p.setId(1);
        p.setName("本科生");
        when(patronCategoryMapper.findAll()).thenReturn(List.of(p));

        mockMvc.perform(get("/api/rules/patron-categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("本科生"));
    }

    @Test
    void getItemTypes_shouldReturnList() throws Exception {
        ItemType t = new ItemType();
        t.setId(1);
        t.setName("普通图书");
        when(itemTypeMapper.findAll()).thenReturn(List.of(t));

        mockMvc.perform(get("/api/rules/item-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("普通图书"));
    }

    @Test
    void upsert_shouldCreateRule() throws Exception {
        doAnswer(inv -> {
            CirculationRule r = inv.getArgument(0);
            r.setId(1);
            return null;
        }).when(ruleMapper).upsert(any(CirculationRule.class));

        mockMvc.perform(put("/api/rules")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"patronCategoryId\":1,\"itemTypeId\":1,\"maxBorrows\":5,\"loanDays\":30,\"renewals\":1,\"renewalDays\":15,\"finePerDay\":0.10}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maxBorrows").value(5));

        verify(ruleMapper).upsert(any(CirculationRule.class));
    }

    @Test
    void upsert_shouldRejectInvalidInput() throws Exception {
        mockMvc.perform(put("/api/rules")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"patronCategoryId\":null,\"itemTypeId\":null}"))
                .andExpect(status().isBadRequest());
    }
}
