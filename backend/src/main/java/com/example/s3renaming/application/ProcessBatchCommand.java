package com.example.s3renaming.application;

import java.util.List;

public record ProcessBatchCommand(
        String bucketName,
        String prefix,
        List<String> files
) {
}

