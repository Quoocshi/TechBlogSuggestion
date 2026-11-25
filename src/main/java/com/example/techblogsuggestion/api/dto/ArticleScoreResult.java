package com.example.techblogsuggestion.api.dto;

public record ArticleScoreResult(
        Long id,
        Double score,
        String reason
) {
}
