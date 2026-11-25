package com.example.techblogsuggestion.service.impl;

import com.example.techblogsuggestion.api.dto.RawArticleItem;
import com.example.techblogsuggestion.domain.model.FeedSource;
import com.example.techblogsuggestion.domain.model.SourceType;
import com.example.techblogsuggestion.service.FeedFetcherStrategy;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RssFeedFetcherStrategy implements FeedFetcherStrategy {

    private final HttpClient httpClient;

    @Override
    public SourceType supportedType() {
        return SourceType.RSS;
    }

    @Override
    public List<RawArticleItem> fetch(FeedSource source) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(source.getRssUrl()))
                .timeout(Duration.ofSeconds(15))
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/128.0.0.0 Safari/537.36")
                .GET()
                .build();

        HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
        if (response.statusCode() != 200) {
            throw new RuntimeException("HTTP error code: " + response.statusCode());
        }

        try (XmlReader reader = new XmlReader(response.body())) {
            SyndFeed feed = new SyndFeedInput().build(reader);
            return feed.getEntries().stream()
                    .filter(e -> e.getLink() != null && !e.getLink().isBlank())
                    .map(e -> {
                        String contentHtml = "";
                        if (e.getContents() != null && !e.getContents().isEmpty()) {
                            contentHtml = e.getContents().get(0).getValue();
                        } else if (e.getDescription() != null) {
                            contentHtml = e.getDescription().getValue();
                        }

                        return new RawArticleItem(
                                e.getLink().trim(),
                                e.getTitle() != null ? e.getTitle().trim() : "Untitled",
                                contentHtml,
                                e.getPublishedDate() != null ? e.getPublishedDate().toInstant() : Instant.now()
                        );
                    })
                    .toList();
        }
    }
}