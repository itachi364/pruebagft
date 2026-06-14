package com.example.s3renaming.domain;

import java.time.Instant;

public record ProcessingBatch(
        String batchId,
        int total,
        int transformed,
        int errors,
        int unmapped,
        Instant startedAt,
        Instant finishedAt
) {
}

