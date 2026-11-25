package com.example.techblogsuggestion.service;

import com.example.techblogsuggestion.api.dto.IngestionSummaryResponse;

public interface IngestionService {
    IngestionSummaryResponse ingestAllActiveSources();
}
