package com.library.service;

import com.library.entity.*;
import com.library.exception.AppException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RuleServiceTest extends AbstractServiceTest {

    private RuleService ruleService;

    @BeforeEach
    void setUp() {
        ruleService = new RuleService(circulationRuleMapper);
    }

    @Test
    void getRule_shouldReturnSpecificRule() {
        CirculationRule rule = new CirculationRule();
        rule.setMaxBorrows(10);
        rule.setLoanDays(60);

        when(circulationRuleMapper.findByPatronAndItemType(1, 2)).thenReturn(rule);

        CirculationRule result = ruleService.getRule(1, 2);
        assertEquals(10, result.getMaxBorrows());
    }

    @Test
    void getRule_shouldFallbackToDefault() {
        when(circulationRuleMapper.findByPatronAndItemType(1, 2)).thenReturn(null);
        CirculationRule defaultRule = new CirculationRule();
        defaultRule.setMaxBorrows(5);
        defaultRule.setLoanDays(30);
        defaultRule.setRenewals(1);

        when(circulationRuleMapper.findDefault()).thenReturn(defaultRule);

        CirculationRule result = ruleService.getRule(1, 2);
        assertEquals(5, result.getMaxBorrows());
    }

    @Test
    void getRule_shouldReturnHardcodedFallbackWhenNoRules() {
        when(circulationRuleMapper.findDefault()).thenReturn(null);

        CirculationRule result = ruleService.getRule(null, null);
        assertNotNull(result);
        assertEquals(5, result.getMaxBorrows());
        assertEquals(30, result.getLoanDays());
    }
}

class CategoryServiceTest extends AbstractServiceTest {

    private CategoryService categoryService;

    @BeforeEach
    void setUp() {
        categoryService = new CategoryService(categoryMapper);
    }

    @Test
    void findAll_shouldReturnCategoriesWithCount() {
        Category cat = new Category();
        cat.setId(1);
        cat.setName("计算机");

        when(categoryMapper.findAll()).thenReturn(List.of(cat));
        when(categoryMapper.countBooksByCategory(1)).thenReturn(5L);

        var result = categoryService.findAll();
        assertEquals(1, result.size());
        assertEquals("计算机", result.get(0).getName());
    }

    @Test
    void create_shouldInsertCategory() {
        doAnswer(inv -> {
            Category c = inv.getArgument(0);
            c.setId(1);
            return null;
        }).when(categoryMapper).insert(any(Category.class));

        Category result = categoryService.create("文学", "文学类图书");
        assertEquals("文学", result.getName());
    }

    @Test
    void delete_shouldRejectWhenBooksExist() {
        when(categoryMapper.countBooksByCategory(1)).thenReturn(3L);
        assertThrows(AppException.class, () -> categoryService.delete(1));
        verify(categoryMapper, never()).deleteById(any());
    }
}
