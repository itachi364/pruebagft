package com.example.s3renaming.api;

import com.example.s3renaming.application.ProcessingService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final ProcessingService processingService;

    public FileController(ProcessingService processingService) {
        this.processingService = processingService;
    }

    @GetMapping
    public FileListResponse listFiles(
            @RequestParam(required = false) String bucketName,
            @RequestParam(required = false, defaultValue = "") String prefix
    ) {
        return new FileListResponse(processingService.listFiles(bucketName, prefix));
    }

    public record FileListResponse(List<String> files) {
    }
}

