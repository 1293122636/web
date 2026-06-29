package com.library.controller;

import com.library.service.BookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookService bookService;

    @Test
    void listBooks_shouldReturnPagedResult() throws Exception {
        when(bookService.list(any())).thenReturn(Map.of(
                "books", List.of(),
                "total", 0L,
                "page", 1,
                "limit", 20,
                "pages", 0
        ));

        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.books").isArray())
                .andExpect(jsonPath("$.total").isNumber());
    }

    @Test
    void listBooks_shouldAcceptSearchParams() throws Exception {
        when(bookService.list(any())).thenReturn(Map.of(
                "books", List.of(),
                "total", 0L,
                "page", 1,
                "limit", 20,
                "pages", 0
        ));

        mockMvc.perform(get("/api/books")
                        .param("search", "算法")
                        .param("categoryId", "1")
                        .param("page", "1")
                        .param("limit", "10"))
                .andExpect(status().isOk());
    }
}
