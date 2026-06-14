package com.example.s3renaming.infrastructure.memory;

import com.example.s3renaming.application.FileStoragePort;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "app.adapters", name = "storage", havingValue = "memory", matchIfMissing = true)
public class InMemoryFileStorageAdapter implements FileStoragePort {

    private final List<String> sampleFiles = new ArrayList<>(List.of(
            "PHO_CD_DES_20260430",
            "PHO_SV_20260430",
            "PHO_CK_20260430",
            "PHO_ML_UTIL_20260430.txt",
            "cuotas_bdb_20260430.txt",
            "garantias_solo_firma_20263004.txt",
            "activos_vehiculo_20260430.txt",
            "PrendasPajaro.txt"
    ));

    @Override
    public List<String> listFiles(String bucketName, String prefix) {
        if (prefix == null || prefix.isBlank()) {
            return List.copyOf(sampleFiles);
        }
        return sampleFiles.stream()
                .filter(file -> file.startsWith(prefix))
                .toList();
    }
}

