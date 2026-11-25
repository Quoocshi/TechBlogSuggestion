package com.example.techblogsuggestion.api.dto;

import java.util.List;

public record ArticleSummaryResult (
        String tldr,
        List<String> keyTakeaways,
        List<String> tags
){
}
