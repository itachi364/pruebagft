package com.example.s3renaming.infrastructure.aws;

import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

@Configuration
@ConditionalOnProperty(prefix = "app.adapters", name = "storage", havingValue = "s3")
public class AwsClientConfig {

    @Bean
    S3Client s3Client(
            @Value("${app.aws.region}") String region,
            @Value("${app.aws.endpoint-url:}") String endpointUrl
    ) {
        S3ClientBuilder builder = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create());
        if (endpointUrl != null && !endpointUrl.isBlank()) {
            builder.endpointOverride(URI.create(endpointUrl));
            builder.forcePathStyle(true);
        }
        return builder.build();
    }
}
