package com.example.s3renaming.application;

import com.example.s3renaming.domain.ProcessingBatch;
import com.example.s3renaming.domain.TransformationResult;
import java.util.List;

public record BatchProcessingResponse(
        ProcessingBatch batch,
        List<TransformationResult> results
) {
}

