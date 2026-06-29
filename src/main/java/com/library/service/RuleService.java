package com.library.service;

import com.library.entity.CirculationRule;
import com.library.mapper.CirculationRuleMapper;
import org.springframework.stereotype.Service;

@Service
public class RuleService {

    private final CirculationRuleMapper ruleMapper;

    public RuleService(CirculationRuleMapper ruleMapper) {
        this.ruleMapper = ruleMapper;
    }

    /**
     * Get the applicable circulation rule for a given patron + item type combination.
     * Falls back: specific rule → default rule → hardcoded defaults.
     */
    public CirculationRule getRule(Integer patronCategoryId, Integer itemTypeId) {
        if (patronCategoryId != null && itemTypeId != null) {
            CirculationRule rule = ruleMapper.findByPatronAndItemType(patronCategoryId, itemTypeId);
            if (rule != null) return rule;
        }

        CirculationRule defaultRule = ruleMapper.findDefault();
        if (defaultRule != null) return defaultRule;

        // Hardcoded fallback
        CirculationRule fallback = new CirculationRule();
        fallback.setMaxBorrows(5);
        fallback.setLoanDays(30);
        fallback.setRenewals(1);
        fallback.setRenewalDays(15);
        fallback.setFinePerDay(java.math.BigDecimal.valueOf(0.10));
        return fallback;
    }
}
