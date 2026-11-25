package com.example.techblogsuggestion.domain.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "feed_sources")
public class FeedSource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "rss_url", nullable = false, unique = true, length = 2048)
    private String rssUrl;

    @Column(nullable = false, length = 100)
    private String category;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "last_fetched_at")
    private Instant lastFetchedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 50)
    private SourceType sourceType = SourceType.RSS;

    public FeedSource(){}
    public FeedSource(String name, String rssUrl, String category) {
        this.name = name;
        this.rssUrl = rssUrl;
        this.category = category;
    }

    public Long getId() {return id;}

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRssUrl() {
        return rssUrl;
    }

    public void setRssUrl(String rssUrl) {
        this.rssUrl = rssUrl;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Instant getLastFetchedAt() {
        return lastFetchedAt;
    }

    public void setLastFetchedAt(Instant lastFetchedAt) {
        this.lastFetchedAt = lastFetchedAt;
    }

    public SourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(SourceType sourceType) {
        this.sourceType = sourceType;
    }

}
