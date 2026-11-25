package com.example.techblogsuggestion.api.dto;

import com.example.techblogsuggestion.domain.model.FeedSource;

import java.time.Instant;

public record FeedSourceResponse(
        Long id,
        String name,
        String rssUrl,
        String category,
        boolean active,
        Instant lastFetchedAt
) {
    public static FeedSourceResponse from(FeedSource entity){
        return new FeedSourceResponse(
                entity.getId(),
                entity.getName(),
                entity.getRssUrl(),
                entity.getCategory(),
                entity.isActive(),
                entity.getLastFetchedAt()
        );
    }
}
