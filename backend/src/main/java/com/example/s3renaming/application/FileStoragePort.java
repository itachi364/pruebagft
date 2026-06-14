package com.example.s3renaming.application;

import java.util.List;

public interface FileStoragePort {

    List<String> listFiles(String bucketName, String prefix);
}

