package com.example.s3renaming.domain;

import java.util.Comparator;
import java.util.List;

public class RuleEngine {

    private final DateNormalizer dateNormalizer;

    public RuleEngine(DateNormalizer dateNormalizer) {
        this.dateNormalizer = dateNormalizer;
    }

    public TransformationResult transform(String batchId, String sourceFileName, List<MappingRule> rules) {
        if (sourceFileName == null || sourceFileName.isBlank()) {
            return TransformationResult.error(batchId, sourceFileName, null, "El nombre de archivo es obligatorio.");
        }
        if (rules == null || rules.isEmpty()) {
            return TransformationResult.unmapped(batchId, sourceFileName, "No hay reglas disponibles.");
        }

        String nameWithoutExtension = FileNameNormalizer.removeExtension(sourceFileName);
        return rules.stream()
                .filter(MappingRule::active)
                .sorted(Comparator.comparingInt(MappingRule::priority))
                .filter(rule -> WildcardPatternMatcher.matches(rule.sourcePattern(), nameWithoutExtension))
                .findFirst()
                .map(rule -> transformWithRule(batchId, sourceFileName, nameWithoutExtension, rule))
                .orElseGet(() -> TransformationResult.unmapped(
                        batchId,
                        sourceFileName,
                        "No existe una regla activa aplicable."
                ));
    }

    private TransformationResult transformWithRule(
            String batchId,
            String sourceFileName,
            String nameWithoutExtension,
            MappingRule rule
    ) {
        if (!rule.requiresDate()) {
            return TransformationResult.transformed(batchId, sourceFileName, renderTarget(rule, null), rule);
        }

        return dateNormalizer.extractCandidate(nameWithoutExtension)
                .map(candidate -> normalizeAndRender(batchId, sourceFileName, rule, candidate))
                .orElseGet(() -> TransformationResult.error(
                        batchId,
                        sourceFileName,
                        rule,
                        "La regla requiere fecha, pero el archivo no contiene una fecha embebida."
                ));
    }

    private TransformationResult normalizeAndRender(
            String batchId,
            String sourceFileName,
            MappingRule rule,
            String candidate
    ) {
        DateNormalizationResult dateResult = dateNormalizer.normalize(candidate, rule.dateStrategy());
        if (!dateResult.valid()) {
            return TransformationResult.error(batchId, sourceFileName, rule, dateResult.message());
        }
        return TransformationResult.transformed(
                batchId,
                sourceFileName,
                renderTarget(rule, dateResult.normalizedDate()),
                rule
        );
    }

    private String renderTarget(MappingRule rule, String normalizedDate) {
        if (normalizedDate == null) {
            return rule.targetTemplate().replace("_{date}", "").replace("{date}", "");
        }
        return rule.targetTemplate().replace("{date}", normalizedDate);
    }
}

