package com.example.s3renaming.domain;

public record MappingRule(
        String ruleId,
        int version,
        String name,
        String sourcePattern,
        String targetTemplate,
        boolean requiresDate,
        DateStrategy dateStrategy,
        int priority,
        boolean active
) {
}

