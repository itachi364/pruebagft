package com.example.s3renaming.domain;

public record DateNormalizationResult(
        boolean valid,
        String normalizedDate,
        DateStrategy strategyUsed,
        String message
) {
    public static DateNormalizationResult valid(String normalizedDate, DateStrategy strategyUsed) {
        return new DateNormalizationResult(true, normalizedDate, strategyUsed, null);
    }

    public static DateNormalizationResult invalid(String message) {
        return new DateNormalizationResult(false, null, null, message);
    }
}

