package com.example.techblogsuggestion.api.dto;

import java.time.Instant;

public record RawArticleItem(
        String url,
        String title,
        String summaryOrSnippet,
        Instant publishedAt
) {
}