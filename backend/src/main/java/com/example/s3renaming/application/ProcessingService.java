package com.example.s3renaming.application;

import com.example.s3renaming.domain.ProcessingBatch;
import com.example.s3renaming.domain.ProcessingStatus;
import com.example.s3renaming.domain.RuleEngine;
import com.example.s3renaming.domain.TransformationResult;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ProcessingService {

    private final FileStoragePort fileStoragePort;
    private final RuleRepository ruleRepository;
    private final ProcessingRepository processingRepository;
    private final RuleEngine ruleEngine;
    private final String defaultBucketName;

    public ProcessingService(
            FileStoragePort fileStoragePort,
            RuleRepository ruleRepository,
            ProcessingRepository processingRepository,
            RuleEngine ruleEngine,
            @Value("${app.s3.bucket-name}") String defaultBucketName
    ) {
        this.fileStoragePort = fileStoragePort;
        this.ruleRepository = ruleRepository;
        this.processingRepository = processingRepository;
        this.ruleEngine = ruleEngine;
        this.defaultBucketName = defaultBucketName;
    }

    public List<String> listFiles(String bucketName, String prefix) {
        return fileStoragePort.listFiles(resolveBucket(bucketName), prefix == null ? "" : prefix);
    }

    public BatchProcessingResponse process(ProcessBatchCommand command) {
        String batchId = UUID.randomUUID().toString();
        Instant startedAt = Instant.now();
        List<String> files = resolveFiles(command);
        List<TransformationResult> results = files.stream()
                .map(file -> ruleEngine.transform(batchId, file, ruleRepository.findActive()))
                .toList();
        ProcessingBatch batch = buildBatch(batchId, startedAt, results);
        processingRepository.saveBatch(batch);
        processingRepository.saveResults(results);
        return new BatchProcessingResponse(batch, results);
    }

    public BatchProcessingResponse reprocess(String batchId) {
        List<String> sourceFiles = processingRepository.findResultsByBatchId(batchId).stream()
                .map(TransformationResult::sourceFileName)
                .toList();
        if (sourceFiles.isEmpty()) {
            throw new IllegalArgumentException("No existen resultados para reprocesar el lote: " + batchId);
        }
        return process(new ProcessBatchCommand(defaultBucketName, "", sourceFiles));
    }

    public ProcessingBatch getSummary(String batchId) {
        return processingRepository.findBatchById(batchId)
                .orElseThrow(() -> new IllegalArgumentException("No existe el lote: " + batchId));
    }

    public List<TransformationResult> getResults(String batchId) {
        return processingRepository.findResultsByBatchId(batchId);
    }

    private List<String> resolveFiles(ProcessBatchCommand command) {
        if (command.files() != null && !command.files().isEmpty()) {
            return command.files();
        }
        return listFiles(command.bucketName(), command.prefix());
    }

    private String resolveBucket(String bucketName) {
        if (bucketName == null || bucketName.isBlank()) {
            return defaultBucketName;
        }
        return bucketName;
    }

    private ProcessingBatch buildBatch(String batchId, Instant startedAt, List<TransformationResult> results) {
        int transformed = countByStatus(results, ProcessingStatus.TRANSFORMADO);
        int errors = countByStatus(results, ProcessingStatus.ERROR);
        int unmapped = countByStatus(results, ProcessingStatus.NO_MAPEADO);
        return new ProcessingBatch(batchId, results.size(), transformed, errors, unmapped, startedAt, Instant.now());
    }

    private int countByStatus(List<TransformationResult> results, ProcessingStatus status) {
        return (int) results.stream().filter(result -> result.status() == status).count();
    }
}

