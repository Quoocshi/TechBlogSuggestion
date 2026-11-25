package com.example.techblogsuggestion.service;

import com.example.techblogsuggestion.api.dto.FeedSourceRequest;
import com.example.techblogsuggestion.api.dto.FeedSourceResponse;

import java.util.List;

public interface FeedSourceService {
    public FeedSourceResponse createSource(FeedSourceRequest request);
    public List<FeedSourceResponse> getAllSources();
    public void toggleActive(Long id);
}
