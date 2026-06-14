package com.example.s3renaming.domain;

public enum ProcessingStatus {
    TRANSFORMADO("Transformado"),
    ERROR("Error"),
    NO_MAPEADO("No mapeado");

    private final String label;

    ProcessingStatus(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}

