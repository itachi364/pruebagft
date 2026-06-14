package com.example.s3renaming.infrastructure.memory;

import com.example.s3renaming.application.ProcessingRepository;
import com.example.s3renaming.domain.ProcessingBatch;
import com.example.s3renaming.domain.TransformationResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(prefix = "app.adapters", name = "persistence", havingValue = "memory", matchIfMissing = true)
public class InMemoryProcessingRepository implements ProcessingRepository {

    private final Map<String, ProcessingBatch> batches = new ConcurrentHashMap<>();
    private final Map<String, List<TransformationResult>> resultsByBatch = new ConcurrentHashMap<>();

    @Override
    public ProcessingBatch saveBatch(ProcessingBatch batch) {
        batches.put(batch.batchId(), batch);
        return batch;
    }

    @Override
    public void saveResults(List<TransformationResult> results) {
        if (results.isEmpty()) {
            return;
        }
        resultsByBatch.computeIfAbsent(results.get(0).batchId(), ignored -> new ArrayList<>()).addAll(results);
    }

    @Override
    public Optional<ProcessingBatch> findBatchById(String batchId) {
        return Optional.ofNullable(batches.get(batchId));
    }

    @Override
    public List<TransformationResult> findResultsByBatchId(String batchId) {
        return List.copyOf(resultsByBatch.getOrDefault(batchId, List.of()));
    }
}

