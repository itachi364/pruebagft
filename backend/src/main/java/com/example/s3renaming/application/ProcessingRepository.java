package com.example.s3renaming.application;

import com.example.s3renaming.domain.ProcessingBatch;
import com.example.s3renaming.domain.TransformationResult;
import java.util.List;
import java.util.Optional;

public interface ProcessingRepository {

    ProcessingBatch saveBatch(ProcessingBatch batch);

    void saveResults(List<TransformationResult> results);

    Optional<ProcessingBatch> findBatchById(String batchId);

    List<TransformationResult> findResultsByBatchId(String batchId);
}

