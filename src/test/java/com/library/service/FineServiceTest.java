package com.library.service;

import com.library.entity.Fine;
import com.library.exception.AppException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class FineServiceTest extends AbstractServiceTest {

    private FineService fineService;

    @BeforeEach
    void setUp() {
        fineService = new FineService(fineMapper, userMapper, auditLogMapper);
    }

    @Test
    void calcOverdueFine_shouldCalculateCorrectAmount() {
        LocalDateTime due = LocalDateTime.now().minusDays(10);
        LocalDateTime now = LocalDateTime.now();
        BigDecimal rate = BigDecimal.valueOf(0.10);

        BigDecimal fine = fineService.calcOverdueFine(due, now, rate);
        assertTrue(fine.compareTo(BigDecimal.ZERO) > 0);
        assertEquals(0, fine.remainder(BigDecimal.valueOf(0.10)).compareTo(BigDecimal.ZERO));
    }

    @Test
    void calcOverdueFine_shouldReturnZeroIfNotOverdue() {
        LocalDateTime due = LocalDateTime.now().plusDays(5);
        BigDecimal fine = fineService.calcOverdueFine(due, LocalDateTime.now(), BigDecimal.valueOf(0.50));
        assertEquals(BigDecimal.ZERO, fine);
    }

    @Test
    void findAll_shouldReturnAllFines() {
        when(fineMapper.findAll(null, null)).thenReturn(List.of(new Fine()));
        assertEquals(1, fineService.findAll(null, null).size());
    }

    @Test
    void findAll_shouldFilterByType() {
        Fine overdue = new Fine();
        overdue.setType("overdue");

        when(fineMapper.findAll("overdue", null)).thenReturn(List.of(overdue));
        List<Fine> result = fineService.findAll("overdue", null);
        assertEquals(1, result.size());
        assertEquals("overdue", result.get(0).getType());
    }

    @Test
    void findMyFines_shouldReturnForUser() {
        when(fineMapper.findByUserId(1)).thenReturn(List.of(new Fine()));
        assertEquals(1, fineService.findByUserId(1).size());
    }

    @Test
    void markPaid_shouldUpdateStatus() {
        Fine existing = new Fine();
        existing.setId(1);
        existing.setUserId(1);
        existing.setAmount(BigDecimal.TEN);
        existing.setPaid(false);

        Fine paid = new Fine();
        paid.setId(1);
        paid.setUserId(1);
        paid.setAmount(BigDecimal.TEN);
        paid.setPaid(true);

        when(fineMapper.findById(1)).thenReturn(existing, paid);

        Fine result = fineService.markPaid(1, 1);
        assertTrue(result.getPaid());
        verify(userMapper).addFine(1, BigDecimal.TEN.negate());
        verify(fineMapper).markPaid(eq(1), any());
    }

    @Test
    void markPaid_shouldThrowIfAlreadyPaid() {
        Fine existing = new Fine();
        existing.setId(1);
        existing.setUserId(1);
        existing.setPaid(true);

        when(fineMapper.findById(1)).thenReturn(existing);
        assertThrows(AppException.class, () -> fineService.markPaid(1, 1));
    }
}
