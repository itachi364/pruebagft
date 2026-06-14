package com.example.s3renaming.application;

import com.example.s3renaming.domain.DateStrategy;
import com.example.s3renaming.domain.MappingRule;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class RuleService {

    private final RuleRepository ruleRepository;

    public RuleService(RuleRepository ruleRepository) {
        this.ruleRepository = ruleRepository;
    }

    public List<MappingRule> listRules() {
        return ruleRepository.findAll().stream()
                .sorted(Comparator.comparing(MappingRule::ruleId).thenComparing(MappingRule::version))
                .toList();
    }

    public MappingRule create(CreateRuleCommand command) {
        MappingRule rule = new MappingRule(
                command.ruleId(),
                1,
                command.name(),
                command.sourcePattern(),
                command.targetTemplate(),
                command.requiresDate(),
                command.dateStrategy(),
                command.priority(),
                command.active()
        );
        return ruleRepository.save(rule);
    }

    public MappingRule update(String ruleId, CreateRuleCommand command) {
        MappingRule previous = ruleRepository.findLatestById(ruleId)
                .orElseThrow(() -> new IllegalArgumentException("La regla no existe: " + ruleId));
        MappingRule updated = new MappingRule(
                previous.ruleId(),
                previous.version() + 1,
                command.name(),
                command.sourcePattern(),
                command.targetTemplate(),
                command.requiresDate(),
                command.dateStrategy(),
                command.priority(),
                command.active()
        );
        return ruleRepository.save(updated);
    }

    public MappingRule changeStatus(String ruleId, boolean active) {
        MappingRule previous = ruleRepository.findLatestById(ruleId)
                .orElseThrow(() -> new IllegalArgumentException("La regla no existe: " + ruleId));
        MappingRule updated = new MappingRule(
                previous.ruleId(),
                previous.version() + 1,
                previous.name(),
                previous.sourcePattern(),
                previous.targetTemplate(),
                previous.requiresDate(),
                previous.dateStrategy(),
                previous.priority(),
                active
        );
        return ruleRepository.save(updated);
    }

    public record CreateRuleCommand(
            String ruleId,
            String name,
            String sourcePattern,
            String targetTemplate,
            boolean requiresDate,
            DateStrategy dateStrategy,
            int priority,
            boolean active
    ) {
    }
}

