package com.example.s3renaming.domain;

public final class FileNameNormalizer {

    private FileNameNormalizer() {
    }

    public static String removeExtension(String sourceFileName) {
        if (sourceFileName == null || sourceFileName.isBlank()) {
            return "";
        }
        int lastSlash = Math.max(sourceFileName.lastIndexOf('/'), sourceFileName.lastIndexOf('\\'));
        int lastDot = sourceFileName.lastIndexOf('.');
        if (lastDot > lastSlash) {
            return sourceFileName.substring(0, lastDot);
        }
        return sourceFileName;
    }
}

