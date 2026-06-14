package com.example.s3renaming.domain;

import java.time.Instant;
import java.util.UUID;

public record TransformationResult(
        String resultId,
        String batchId,
        String sourceFileName,
        String targetFileName,
        ProcessingStatus status,
        String statusLabel,
        String ruleId,
        Integer ruleVersion,
        String message,
        Instant processedAt
) {
    public static TransformationResult transformed(
            String batchId,
            String sourceFileName,
            String targetFileName,
            MappingRule rule
    ) {
        return new TransformationResult(
                UUID.randomUUID().toString(),
                batchId,
                sourceFileName,
                targetFileName,
                ProcessingStatus.TRANSFORMADO,
                ProcessingStatus.TRANSFORMADO.label(),
                rule.ruleId(),
                rule.version(),
                "Archivo transformado correctamente.",
                Instant.now()
        );
    }

    public static TransformationResult unmapped(String batchId, String sourceFileName, String message) {
        return new TransformationResult(
                UUID.randomUUID().toString(),
                batchId,
                sourceFileName,
                null,
                ProcessingStatus.NO_MAPEADO,
                ProcessingStatus.NO_MAPEADO.label(),
                null,
                null,
                message,
                Instant.now()
        );
    }

    public static TransformationResult error(String batchId, String sourceFileName, MappingRule rule, String message) {
        return new TransformationResult(
                UUID.randomUUID().toString(),
                batchId,
                sourceFileName,
                null,
                ProcessingStatus.ERROR,
                ProcessingStatus.ERROR.label(),
                rule == null ? null : rule.ruleId(),
                rule == null ? null : rule.version(),
                message,
                Instant.now()
        );
    }
}

