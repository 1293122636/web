package com.library.service;

import com.library.entity.BorrowRecord;
import com.library.entity.CirculationRule;
import com.library.mapper.BorrowRecordMapper;
import com.library.mapper.FineMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class OverdueFineScheduler {

    private static final Logger log = LoggerFactory.getLogger(OverdueFineScheduler.class);

    private final BorrowRecordMapper borrowRecordMapper;
    private final FineMapper fineMapper;
    private final FineService fineService;
    private final RuleService ruleService;

    public OverdueFineScheduler(BorrowRecordMapper borrowRecordMapper,
                                FineMapper fineMapper,
                                FineService fineService,
                                RuleService ruleService) {
        this.borrowRecordMapper = borrowRecordMapper;
        this.fineMapper = fineMapper;
        this.fineService = fineService;
        this.ruleService = ruleService;
    }

    /**
     * Runs daily at 2:00 AM to auto-create fines for overdue borrows.
     * Idempotent: skips borrow records that already have a fine.
     */
    @Transactional
    @Scheduled(cron = "0 0 2 * * ?")
    public void autoFineOverdueBorrows() {
        log.info("Starting overdue fine auto-creation task");
        List<BorrowRecord> overdueRecords = borrowRecordMapper.findOverdueBorrows();
        int created = 0;
        int skipped = 0;

        for (BorrowRecord record : overdueRecords) {
            try {
                // Idempotency check: skip if a fine already exists for this borrow
                Integer existingFineId = fineMapper.findByBorrowRecordId(record.getId());
                if (existingFineId != null) {
                    skipped++;
                    continue;
                }

                LocalDateTime now = LocalDateTime.now();
                long daysOverdue = ChronoUnit.DAYS.between(record.getDueDate(), now);
                if (daysOverdue <= 0) {
                    skipped++;
                    continue;
                }

                CirculationRule rule = ruleService.getRule(null, null);
                BigDecimal fineAmount = rule.getFinePerDay().multiply(BigDecimal.valueOf(daysOverdue));

                fineService.createFine(record.getId(), record.getUserId(), fineAmount, "overdue");
                created++;
            } catch (Exception e) {
                log.error("Failed to auto-create fine for borrowRecordId={}", record.getId(), e);
            }
        }

        if (created > 0 || skipped > 0) {
            log.info("Overdue fine task complete: created={}, skipped={}, total={}", created, skipped, overdueRecords.size());
        }
    }
}
