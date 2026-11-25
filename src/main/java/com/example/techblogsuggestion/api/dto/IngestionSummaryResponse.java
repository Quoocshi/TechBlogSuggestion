package com.example.techblogsuggestion.api.dto;

public record IngestionSummaryResponse(
        int sourcesProcessed,
        int newArticlesSaved,
        int duplicatesSkipped,
        int failedSources
){}
