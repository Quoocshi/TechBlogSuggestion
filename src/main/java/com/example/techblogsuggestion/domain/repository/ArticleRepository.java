package com.example.techblogsuggestion.domain.repository;

import com.example.techblogsuggestion.domain.model.Article;
import com.example.techblogsuggestion.domain.model.ArticleStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface ArticleRepository extends JpaRepository<Article, Long> {
    boolean existsByUrl(String url);
    List<Article> findAllByStatus(ArticleStatus status);
    List<Article> findAllByStatusAndCreatedAtAfter(ArticleStatus status, Instant after);
    List<Article> findAllByStatusOrderByRelevanceScoreDesc(ArticleStatus status);
}