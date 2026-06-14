package com.example.s3renaming.api;

import com.example.s3renaming.application.RuleService;
import com.example.s3renaming.domain.DateStrategy;
import com.example.s3renaming.domain.MappingRule;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rules")
public class RuleController {

    private final RuleService ruleService;

    public RuleController(RuleService ruleService) {
        this.ruleService = ruleService;
    }

    @GetMapping
    public List<MappingRule> listRules() {
        return ruleService.listRules();
    }

    @PostMapping
    public MappingRule create(@Valid @RequestBody RuleRequest request) {
        return ruleService.create(request.toCommand());
    }

    @PutMapping("/{ruleId}")
    public MappingRule update(@PathVariable String ruleId, @Valid @RequestBody RuleRequest request) {
        return ruleService.update(ruleId, request.toCommand());
    }

    @PatchMapping("/{ruleId}/status")
    public MappingRule changeStatus(@PathVariable String ruleId, @Valid @RequestBody RuleStatusRequest request) {
        return ruleService.changeStatus(ruleId, request.active());
    }

    public record RuleRequest(
            @NotBlank String ruleId,
            @NotBlank String name,
            @NotBlank String sourcePattern,
            @NotBlank String targetTemplate,
            boolean requiresDate,
            @NotNull DateStrategy dateStrategy,
            @Min(1) int priority,
            boolean active
    ) {
        RuleService.CreateRuleCommand toCommand() {
            return new RuleService.CreateRuleCommand(
                    ruleId,
                    name,
                    sourcePattern,
                    targetTemplate,
                    requiresDate,
                    dateStrategy,
                    priority,
                    active
            );
        }
    }

    public record RuleStatusRequest(boolean active) {
    }
}

