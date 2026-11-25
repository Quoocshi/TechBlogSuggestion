package com.example.techblogsuggestion.domain.repository;

import com.example.techblogsuggestion.domain.model.FeedSource;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FeedSourceRepository extends JpaRepository<FeedSource, Long> {
    boolean existsByRssUrl(String rssUrl);
    List<FeedSource> findAllByActiveTrue();
}