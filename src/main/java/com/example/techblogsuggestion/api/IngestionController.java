package com.example.techblogsuggestion.api;

import com.example.techblogsuggestion.api.dto.IngestionSummaryResponse;
import com.example.techblogsuggestion.service.IngestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/ingest")
public class IngestionController {

    private final IngestionService rssIngestionService;

    @PostMapping("/trigger")
    public ResponseEntity<IngestionSummaryResponse> triggerIngestion() {
        return ResponseEntity.ok(rssIngestionService.ingestAllActiveSources());
    }
}
