package com.example.techblogsuggestion.service;

import com.example.techblogsuggestion.api.dto.RawArticleItem;
import com.example.techblogsuggestion.domain.model.FeedSource;
import com.example.techblogsuggestion.domain.model.SourceType;

import java.util.List;

public interface FeedFetcherStrategy {
    SourceType supportedType();
    List<RawArticleItem> fetch(FeedSource source) throws Exception;
}