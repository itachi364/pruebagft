package com.example.s3renaming.infrastructure.aws;

import com.example.s3renaming.application.FileStoragePort;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;

@Component
@ConditionalOnProperty(prefix = "app.adapters", name = "storage", havingValue = "s3")
public class S3FileStorageAdapter implements FileStoragePort {

    private final S3Client s3Client;

    public S3FileStorageAdapter(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    @Override
    public List<String> listFiles(String bucketName, String prefix) {
        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(prefix == null ? "" : prefix)
                .build();
        return s3Client.listObjectsV2(request).contents().stream()
                .map(s3Object -> s3Object.key())
                .toList();
    }
}

