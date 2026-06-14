package com.example.s3renaming.infrastructure.aws;

import com.example.s3renaming.application.RuleRepository;
import com.example.s3renaming.domain.DateStrategy;
import com.example.s3renaming.domain.MappingRule;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;

@Repository
@ConditionalOnProperty(prefix = "app.adapters", name = "persistence", havingValue = "dynamodb")
public class DynamoDbRuleRepository implements RuleRepository {

    private final DynamoDbClient dynamoDbClient;
    private final String tableName;

    public DynamoDbRuleRepository(
            DynamoDbClient dynamoDbClient,
            @Value("${app.dynamodb.rules-table-name}") String tableName
    ) {
        this.dynamoDbClient = dynamoDbClient;
        this.tableName = tableName;
    }

    @Override
    public List<MappingRule> findAll() {
        return dynamoDbClient.scan(ScanRequest.builder().tableName(tableName).build()).items().stream()
                .map(this::toRule)
                .toList();
    }

    @Override
    public List<MappingRule> findActive() {
        return findAll().stream()
                .filter(MappingRule::active)
                .sorted(Comparator.comparingInt(MappingRule::priority))
                .toList();
    }

    @Override
    public Optional<MappingRule> findLatestById(String ruleId) {
        return findAll().stream()
                .filter(rule -> rule.ruleId().equals(ruleId))
                .max(Comparator.comparingInt(MappingRule::version));
    }

    @Override
    public MappingRule save(MappingRule rule) {
        dynamoDbClient.putItem(PutItemRequest.builder()
                .tableName(tableName)
                .item(toItem(rule))
                .build());
        return rule;
    }

    private Map<String, AttributeValue> toItem(MappingRule rule) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("ruleId", AttributeValue.fromS(rule.ruleId()));
        item.put("version", AttributeValue.fromN(Integer.toString(rule.version())));
        item.put("name", AttributeValue.fromS(rule.name()));
        item.put("sourcePattern", AttributeValue.fromS(rule.sourcePattern()));
        item.put("targetTemplate", AttributeValue.fromS(rule.targetTemplate()));
        item.put("requiresDate", AttributeValue.fromBool(rule.requiresDate()));
        item.put("dateStrategy", AttributeValue.fromS(rule.dateStrategy().name()));
        item.put("priority", AttributeValue.fromN(Integer.toString(rule.priority())));
        item.put("active", AttributeValue.fromBool(rule.active()));
        return item;
    }

    private MappingRule toRule(Map<String, AttributeValue> item) {
        return new MappingRule(
                item.get("ruleId").s(),
                Integer.parseInt(item.get("version").n()),
                item.get("name").s(),
                item.get("sourcePattern").s(),
                item.get("targetTemplate").s(),
                item.get("requiresDate").bool(),
                DateStrategy.valueOf(item.get("dateStrategy").s()),
                Integer.parseInt(item.get("priority").n()),
                item.get("active").bool()
        );
    }
}

