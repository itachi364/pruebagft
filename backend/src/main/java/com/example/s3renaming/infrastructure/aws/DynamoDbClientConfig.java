package com.example.s3renaming.infrastructure.aws;

import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClientBuilder;

@Configuration
@ConditionalOnProperty(prefix = "app.adapters", name = "persistence", havingValue = "dynamodb")
public class DynamoDbClientConfig {

    @Bean
    DynamoDbClient dynamoDbClient(
            @Value("${app.aws.region}") String region,
            @Value("${app.aws.endpoint-url:}") String endpointUrl
    ) {
        DynamoDbClientBuilder builder = DynamoDbClient.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create());
        if (endpointUrl != null && !endpointUrl.isBlank()) {
            builder.endpointOverride(URI.create(endpointUrl));
        }
        return builder.build();
    }
}

