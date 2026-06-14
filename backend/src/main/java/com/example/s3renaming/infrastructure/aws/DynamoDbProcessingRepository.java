package com.example.s3renaming.infrastructure.aws;

import com.example.s3renaming.application.ProcessingRepository;
import com.example.s3renaming.domain.ProcessingBatch;
import com.example.s3renaming.domain.ProcessingStatus;
import com.example.s3renaming.domain.TransformationResult;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;

@Repository
@ConditionalOnProperty(prefix = "app.adapters", name = "persistence", havingValue = "dynamodb")
public class DynamoDbProcessingRepository implements ProcessingRepository {

    private final DynamoDbClient dynamoDbClient;
    private final String batchesTableName;
    private final String resultsTableName;

    public DynamoDbProcessingRepository(
            DynamoDbClient dynamoDbClient,
            @Value("${app.dynamodb.batches-table-name}") String batchesTableName,
            @Value("${app.dynamodb.results-table-name}") String resultsTableName
    ) {
        this.dynamoDbClient = dynamoDbClient;
        this.batchesTableName = batchesTableName;
        this.resultsTableName = resultsTableName;
    }

    @Override
    public ProcessingBatch saveBatch(ProcessingBatch batch) {
        dynamoDbClient.putItem(PutItemRequest.builder()
                .tableName(batchesTableName)
                .item(toBatchItem(batch))
                .build());
        return batch;
    }

    @Override
    public void saveResults(List<TransformationResult> results) {
        results.forEach(result -> dynamoDbClient.putItem(PutItemRequest.builder()
                .tableName(resultsTableName)
                .item(toResultItem(result))
                .build()));
    }

    @Override
    public Optional<ProcessingBatch> findBatchById(String batchId) {
        Map<String, AttributeValue> key = Map.of("batchId", AttributeValue.fromS(batchId));
        Map<String, AttributeValue> item = dynamoDbClient.getItem(GetItemRequest.builder()
                .tableName(batchesTableName)
                .key(key)
                .build()).item();
        if (item == null || item.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(toBatch(item));
    }

    @Override
    public List<TransformationResult> findResultsByBatchId(String batchId) {
        return dynamoDbClient.query(QueryRequest.builder()
                        .tableName(resultsTableName)
                        .keyConditionExpression("batchId = :batchId")
                        .expressionAttributeValues(Map.of(":batchId", AttributeValue.fromS(batchId)))
                        .build())
                .items()
                .stream()
                .map(this::toResult)
                .toList();
    }

    private Map<String, AttributeValue> toBatchItem(ProcessingBatch batch) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("batchId", AttributeValue.fromS(batch.batchId()));
        item.put("total", AttributeValue.fromN(Integer.toString(batch.total())));
        item.put("transformed", AttributeValue.fromN(Integer.toString(batch.transformed())));
        item.put("errors", AttributeValue.fromN(Integer.toString(batch.errors())));
        item.put("unmapped", AttributeValue.fromN(Integer.toString(batch.unmapped())));
        item.put("startedAt", AttributeValue.fromS(batch.startedAt().toString()));
        item.put("finishedAt", AttributeValue.fromS(batch.finishedAt().toString()));
        return item;
    }

    private ProcessingBatch toBatch(Map<String, AttributeValue> item) {
        return new ProcessingBatch(
                item.get("batchId").s(),
                Integer.parseInt(item.get("total").n()),
                Integer.parseInt(item.get("transformed").n()),
                Integer.parseInt(item.get("errors").n()),
                Integer.parseInt(item.get("unmapped").n()),
                Instant.parse(item.get("startedAt").s()),
                Instant.parse(item.get("finishedAt").s())
        );
    }

    private Map<String, AttributeValue> toResultItem(TransformationResult result) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("batchId", AttributeValue.fromS(result.batchId()));
        item.put("resultId", AttributeValue.fromS(result.resultId()));
        item.put("sourceFileName", AttributeValue.fromS(result.sourceFileName()));
        putNullable(item, "targetFileName", result.targetFileName());
        item.put("status", AttributeValue.fromS(result.status().name()));
        item.put("statusLabel", AttributeValue.fromS(result.statusLabel()));
        putNullable(item, "ruleId", result.ruleId());
        if (result.ruleVersion() != null) {
            item.put("ruleVersion", AttributeValue.fromN(Integer.toString(result.ruleVersion())));
        }
        putNullable(item, "message", result.message());
        item.put("processedAt", AttributeValue.fromS(result.processedAt().toString()));
        return item;
    }

    private TransformationResult toResult(Map<String, AttributeValue> item) {
        ProcessingStatus status = ProcessingStatus.valueOf(item.get("status").s());
        return new TransformationResult(
                item.get("resultId").s(),
                item.get("batchId").s(),
                item.get("sourceFileName").s(),
                stringValue(item, "targetFileName"),
                status,
                item.get("statusLabel").s(),
                stringValue(item, "ruleId"),
                item.containsKey("ruleVersion") ? Integer.parseInt(item.get("ruleVersion").n()) : null,
                stringValue(item, "message"),
                Instant.parse(item.get("processedAt").s())
        );
    }

    private void putNullable(Map<String, AttributeValue> item, String key, String value) {
        if (value != null) {
            item.put(key, AttributeValue.fromS(value));
        }
    }

    private String stringValue(Map<String, AttributeValue> item, String key) {
        return item.containsKey(key) ? item.get(key).s() : null;
    }
}

