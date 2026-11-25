package com.example.techblogsuggestion.api.dto;

import java.util.List;

public record CurationSummaryResponse(
        int totalRawProcessed,
        int duplicatesMarked,
        int topArticlesSummarized,
        int quickLinksRemaining,
        List<TopArticlePreview> top5Preview
) {
    public record TopArticlePreview(
            Long id,
            String title,
            double score,
            String reason,
            String tags
    ) {
    }
}