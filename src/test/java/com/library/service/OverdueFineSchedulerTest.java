package com.library.service;

import com.library.entity.BorrowRecord;
import com.library.entity.CirculationRule;
import com.library.mapper.BorrowRecordMapper;
import com.library.mapper.FineMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OverdueFineSchedulerTest {

    @Mock
    private BorrowRecordMapper borrowRecordMapper;
    @Mock
    private FineMapper fineMapper;
    @Mock
    private FineService fineService;
    @Mock
    private RuleService ruleService;

    private OverdueFineScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new OverdueFineScheduler(borrowRecordMapper, fineMapper, fineService, ruleService);
    }

    @Test
    void autoFineOverdueBorrows_shouldCreateFinesForOverdueRecords() {
        BorrowRecord overdue = new BorrowRecord();
        overdue.setId(1);
        overdue.setUserId(1);
        overdue.setDueDate(LocalDateTime.now().minusDays(10));

        when(borrowRecordMapper.findOverdueBorrows()).thenReturn(List.of(overdue));
        when(fineMapper.findByBorrowRecordId(1)).thenReturn(null);
        when(ruleService.getRule(null, null)).thenReturn(createRule(BigDecimal.valueOf(0.50)));

        scheduler.autoFineOverdueBorrows();

        verify(fineService).createFine(eq(1), eq(1), any(BigDecimal.class), eq("overdue"));
    }

    @Test
    void autoFineOverdueBorrows_shouldSkipWhenFineExists() {
        BorrowRecord overdue = new BorrowRecord();
        overdue.setId(1);
        overdue.setUserId(1);
        overdue.setDueDate(LocalDateTime.now().minusDays(5));

        when(borrowRecordMapper.findOverdueBorrows()).thenReturn(List.of(overdue));
        when(fineMapper.findByBorrowRecordId(1)).thenReturn(999); // already has fine

        scheduler.autoFineOverdueBorrows();

        verify(fineService, never()).createFine(any(), any(), any(), any());
    }

    @Test
    void autoFineOverdueBorrows_shouldSkipWhenNotOverdue() {
        when(borrowRecordMapper.findOverdueBorrows()).thenReturn(List.of());

        scheduler.autoFineOverdueBorrows();

        verify(fineService, never()).createFine(any(), any(), any(), any());
    }

    @Test
    void autoFineOverdueBorrows_shouldHandleEmptyList() {
        when(borrowRecordMapper.findOverdueBorrows()).thenReturn(List.of());

        scheduler.autoFineOverdueBorrows();

        verify(fineService, never()).createFine(any(), any(), any(), any());
    }

    @Test
    void autoFineOverdueBorrows_shouldSkipOnException() {
        BorrowRecord overdue = new BorrowRecord();
        overdue.setId(1);
        overdue.setUserId(1);
        overdue.setDueDate(LocalDateTime.now().minusDays(3));

        when(borrowRecordMapper.findOverdueBorrows()).thenReturn(List.of(overdue));
        when(fineMapper.findByBorrowRecordId(1)).thenReturn(null);
        when(ruleService.getRule(null, null)).thenThrow(new RuntimeException("DB error"));

        // Should not propagate exception
        scheduler.autoFineOverdueBorrows();

        verify(fineService, never()).createFine(any(), any(), any(), any());
    }

    @Test
    void autoFineOverdueBorrows_shouldCalculateCorrectAmount() {
        BorrowRecord overdue = new BorrowRecord();
        overdue.setId(1);
        overdue.setUserId(1);
        overdue.setDueDate(LocalDateTime.now().minusDays(7));

        when(borrowRecordMapper.findOverdueBorrows()).thenReturn(List.of(overdue));
        when(fineMapper.findByBorrowRecordId(1)).thenReturn(null);
        when(ruleService.getRule(null, null)).thenReturn(createRule(BigDecimal.valueOf(0.10)));

        scheduler.autoFineOverdueBorrows();

        // 7 days overdue * 0.10/day = 0.70
        verify(fineService).createFine(eq(1), eq(1), eq(BigDecimal.valueOf(0.70)), eq("overdue"));
    }

    @Test
    void autoFineOverdueBorrows_shouldProcessMultipleRecords() {
        BorrowRecord r1 = new BorrowRecord();
        r1.setId(1);
        r1.setUserId(1);
        r1.setDueDate(LocalDateTime.now().minusDays(5));

        BorrowRecord r2 = new BorrowRecord();
        r2.setId(2);
        r2.setUserId(2);
        r2.setDueDate(LocalDateTime.now().minusDays(3));

        BorrowRecord r3 = new BorrowRecord();
        r3.setId(3);
        r3.setDueDate(LocalDateTime.now().minusDays(1));

        when(borrowRecordMapper.findOverdueBorrows()).thenReturn(List.of(r1, r2, r3));
        when(fineMapper.findByBorrowRecordId(anyInt())).thenReturn(null);
        when(ruleService.getRule(null, null)).thenReturn(createRule(BigDecimal.valueOf(0.10)));

        scheduler.autoFineOverdueBorrows();

        verify(fineService, times(3)).createFine(any(), any(), any(), eq("overdue"));
    }

    private CirculationRule createRule(BigDecimal finePerDay) {
        CirculationRule rule = new CirculationRule();
        rule.setMaxBorrows(5);
        rule.setLoanDays(30);
        rule.setRenewals(1);
        rule.setRenewalDays(15);
        rule.setFinePerDay(finePerDay);
        return rule;
    }
}
