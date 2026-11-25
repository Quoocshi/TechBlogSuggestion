package com.example.techblogsuggestion.api.dto;

import com.example.techblogsuggestion.domain.model.SourceType;
import org.hibernate.validator.constraints.URL;

public record FeedSourceRequest(
        String name,
        @URL(message = "Invalid RSS URL format")
        String rssUrl,
        String category,
        SourceType sourceType
) {
}