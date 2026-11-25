package com.example.techblogsuggestion.service.impl;

import com.example.techblogsuggestion.api.dto.IngestionSummaryResponse;
import com.example.techblogsuggestion.api.dto.RawArticleItem;
import com.example.techblogsuggestion.domain.model.Article;
import com.example.techblogsuggestion.domain.model.FeedSource;
import com.example.techblogsuggestion.domain.model.SourceType;
import com.example.techblogsuggestion.domain.repository.ArticleRepository;
import com.example.techblogsuggestion.domain.repository.FeedSourceRepository;
import com.example.techblogsuggestion.service.ArticleContentExtractor;
import com.example.techblogsuggestion.service.FeedFetcherStrategy;
import com.example.techblogsuggestion.service.IngestionService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class IngestionServiceImpl implements IngestionService {
    private static final Logger log = LoggerFactory.getLogger(IngestionServiceImpl.class);

    private final FeedSourceRepository feedSourceRepository;
    private final ArticleRepository articleRepository;
    private final ArticleContentExtractor contentExtractor;
    private final Map<SourceType, FeedFetcherStrategy> strategyMap;

    public IngestionServiceImpl(FeedSourceRepository feedSourceRepository, ArticleRepository articleRepository, ArticleContentExtractor contentExtractor, List<FeedFetcherStrategy> strategies) {
        this.feedSourceRepository = feedSourceRepository;
        this.articleRepository = articleRepository;
        this.contentExtractor = contentExtractor;
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(FeedFetcherStrategy::supportedType, Function.identity()));
    }

    @Override
    @Transactional
    public IngestionSummaryResponse ingestAllActiveSources() {
        List<FeedSource> activeSources = feedSourceRepository.findAllByActiveTrue();
        int duplicates = 0;
        int newArticles = 0;
        int failed = 0;
        for(FeedSource source : activeSources){
            try{
                FeedFetcherStrategy fetcher = strategyMap.get(source.getSourceType());
                if (fetcher == null) {
                    throw new IllegalStateException("No fetcher found for type: " + source.getSourceType());
                }
                List<RawArticleItem> items = fetcher.fetch(source);
                for(RawArticleItem item : items){
                    if (articleRepository.existsByUrl(item.url())) {
                        duplicates++;
                        continue;
                    }
                    String cleanContent = contentExtractor.extractCleanText(item.url(), item.summaryOrSnippet());

                    Article article = new Article(
                            source,
                            item.url(),
                            item.title(),
                            cleanContent,
                            item.publishedAt()
                    );
                    articleRepository.save(article);
                    newArticles++;
                }

                source.setLastFetchedAt(Instant.now());
                feedSourceRepository.save(source);
            }catch (Exception e){
                failed++;
                log.error("Failed to ingest RSS source [{} - {}]: {}", source.getName(), source.getRssUrl(), e.getMessage());
            }
        }
        return new IngestionSummaryResponse(activeSources.size(), newArticles, duplicates, failed);
    }

}
