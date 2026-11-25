package com.example.techblogsuggestion.service;

import com.example.techblogsuggestion.api.dto.CurationSummaryResponse;

public interface ArticleCurationService {
    CurationSummaryResponse curateRawArticles();
}