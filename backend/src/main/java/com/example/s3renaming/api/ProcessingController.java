package com.example.s3renaming.api;

import com.example.s3renaming.application.BatchProcessingResponse;
import com.example.s3renaming.application.ProcessBatchCommand;
import com.example.s3renaming.application.ProcessingService;
import com.example.s3renaming.domain.ProcessingBatch;
import com.example.s3renaming.domain.TransformationResult;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/processing/batches")
public class ProcessingController {

    private final ProcessingService processingService;

    public ProcessingController(ProcessingService processingService) {
        this.processingService = processingService;
    }

    @PostMapping
    public BatchProcessingResponse process(@Valid @RequestBody ProcessBatchRequest request) {
        return processingService.process(new ProcessBatchCommand(
                request.bucketName(),
                request.prefix(),
                request.files()
        ));
    }

    @PostMapping("/{batchId}/reprocess")
    public BatchProcessingResponse reprocess(@PathVariable String batchId) {
        return processingService.reprocess(batchId);
    }

    @GetMapping("/{batchId}/summary")
    public ProcessingBatch summary(@PathVariable String batchId) {
        return processingService.getSummary(batchId);
    }

    @GetMapping("/{batchId}/results")
    public List<TransformationResult> results(@PathVariable String batchId) {
        return processingService.getResults(batchId);
    }

    public record ProcessBatchRequest(
            String bucketName,
            String prefix,
            List<String> files
    ) {
    }
}

