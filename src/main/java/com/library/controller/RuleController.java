package com.library.controller;

import com.library.entity.PatronCategory;
import com.library.entity.ItemType;
import com.library.entity.CirculationRule;
import com.library.mapper.PatronCategoryMapper;
import com.library.mapper.ItemTypeMapper;
import com.library.mapper.CirculationRuleMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rules")
public class RuleController {

    private final CirculationRuleMapper ruleMapper;
    private final PatronCategoryMapper patronCategoryMapper;
    private final ItemTypeMapper itemTypeMapper;

    public RuleController(CirculationRuleMapper ruleMapper,
                          PatronCategoryMapper patronCategoryMapper,
                          ItemTypeMapper itemTypeMapper) {
        this.ruleMapper = ruleMapper;
        this.patronCategoryMapper = patronCategoryMapper;
        this.itemTypeMapper = itemTypeMapper;
    }

    @GetMapping
    public ResponseEntity<List<CirculationRule>> list() {
        return ResponseEntity.ok(ruleMapper.findAll());
    }

    @GetMapping("/patron-categories")
    public ResponseEntity<List<PatronCategory>> getPatronCategories() {
        return ResponseEntity.ok(patronCategoryMapper.findAll());
    }

    @GetMapping("/item-types")
    public ResponseEntity<List<ItemType>> getItemTypes() {
        return ResponseEntity.ok(itemTypeMapper.findAll());
    }

    @PutMapping
    public ResponseEntity<?> upsert(@RequestBody CirculationRule rule) {
        ruleMapper.upsert(rule);
        return ResponseEntity.ok(rule);
    }
}
