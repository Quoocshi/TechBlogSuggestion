package com.example.techblogsuggestion.service.impl;

import com.example.techblogsuggestion.api.dto.FeedSourceRequest;
import com.example.techblogsuggestion.api.dto.FeedSourceResponse;
import com.example.techblogsuggestion.domain.model.FeedSource;
import com.example.techblogsuggestion.domain.model.SourceType;
import com.example.techblogsuggestion.domain.repository.FeedSourceRepository;
import com.example.techblogsuggestion.service.FeedSourceService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedSourceServiceImpl implements FeedSourceService {
    private final FeedSourceRepository feedSourceRepository;

    @Override
    @Transactional
    public FeedSourceResponse createSource(FeedSourceRequest request) {
        String normalizedUrl = request.rssUrl().trim();
        if (feedSourceRepository.existsByRssUrl(request.rssUrl())) {
            throw new IllegalArgumentException("RSS url already exists" + request.rssUrl());
        }
        FeedSource feedSource = new FeedSource(
                request.name().trim(),
                request.rssUrl().trim(),
                request.category().trim().toUpperCase()
        );

        feedSource.setSourceType(resolveSourceType(request, normalizedUrl));

        FeedSource saved = feedSourceRepository.save(feedSource);
        return FeedSourceResponse.from(saved);
    }

    @Override
    @Transactional
    public List<FeedSourceResponse> getAllSources() {
        return feedSourceRepository.findAll()
                .stream()
                .map(FeedSourceResponse::from)
                .toList();
    }

    @Override
    public void toggleActive(Long id) {
        FeedSource feedSource = feedSourceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("FeedSource not found with id " + id));
        feedSource.setActive(!feedSource.isActive());

    }

    private SourceType resolveSourceType(FeedSourceRequest request, String url) {
        if (request.sourceType() != null) {
            return request.sourceType();
        }
        if (url.contains("uber.com/")) {
            return SourceType.UBER_HTML;
        }
        return SourceType.RSS;
    }
}
