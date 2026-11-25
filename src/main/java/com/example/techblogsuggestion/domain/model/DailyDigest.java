package com.example.techblogsuggestion.domain.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "daily_digests")
public class DailyDigest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "digest_date", nullable = false, unique = true)
    private LocalDate digestDate;

    @Column(name = "compiled_content", nullable = false, columnDefinition = "TEXT")
    private String compiledContent;

    @Column(name = "article_count", nullable = false)
    private int articleCount;

    @Column(name = "sent_at", nullable = false, updatable = false)
    private Instant sentAt;

    public DailyDigest() {
    }

    public DailyDigest(LocalDate digestDate, String compiledContent, int articleCount) {
        this.digestDate = digestDate;
        this.compiledContent = compiledContent;
        this.articleCount = articleCount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDigestDate() {
        return digestDate;
    }

    public void setDigestDate(LocalDate digestDate) {
        this.digestDate = digestDate;
    }

    public String getCompiledContent() {
        return compiledContent;
    }

    public void setCompiledContent(String compiledContent) {
        this.compiledContent = compiledContent;
    }

    public int getArticleCount() {
        return articleCount;
    }

    public void setArticleCount(int articleCount) {
        this.articleCount = articleCount;
    }

    public Instant getSentAt() {
        return sentAt;
    }

    public void setSentAt(Instant sentAt) {
        this.sentAt = sentAt;
    }
}
