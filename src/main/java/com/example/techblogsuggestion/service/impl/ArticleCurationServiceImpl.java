package com.example.techblogsuggestion.service.impl;

import com.example.techblogsuggestion.api.dto.ArticleScoreResult;
import com.example.techblogsuggestion.api.dto.ArticleSummaryResult;
import com.example.techblogsuggestion.api.dto.CurationSummaryResponse;
import com.example.techblogsuggestion.domain.model.Article;
import com.example.techblogsuggestion.domain.model.ArticleStatus;
import com.example.techblogsuggestion.domain.repository.ArticleRepository;
import com.example.techblogsuggestion.domain.repository.ArticleVectorRepository;
import com.example.techblogsuggestion.infrastructure.GeminiClient;
import com.example.techblogsuggestion.service.ArticleCurationService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArticleCurationServiceImpl implements ArticleCurationService {

    private static final Logger log = LoggerFactory.getLogger(ArticleCurationServiceImpl.class);
    private static final double SIMILARITY_THRESHOLD = 0.88;
    private static final int TOP_N = 5;

    private static final String SCORING_SYSTEM_PROMPT = """
            You are a Principal Backend Software Engineer curating a daily technical digest for a Java & Distributed Systems engineer.
            Evaluate each article from 1.0 to 10.0 based on:
            1. Engineering Depth (40%): Deep dives into system architecture, scalability, performance optimization, memory/GC tuning, or real-world post-mortems. Penalize marketing fluff or basic tutorials.
            2. Tech Stack Relevance (30%): Relevance to Backend Engineering, Java/JVM, Distributed Systems, Databases (SQL/NoSQL/Vector), Caching, Messaging, and Production AI Engineering.
            3. Actionable Insights (20%): Concrete benchmarks, architectural trade-offs, and practical lessons.
            4. Novelty (10%): Fresh engineering approaches.

            Return ONLY a valid JSON array matching this exact schema:
            [
              {"id": 12, "score": 9.2, "reason": "One concise sentence explaining the engineering value."}
            ]
            """;

    private static final String SUMMARIZE_SYSTEM_PROMPT = """
            You are a Senior Backend Engineer writing concise technical summaries for busy developers.
            Summarize the provided engineering blog post into a strict JSON object with this exact schema:
            {
              "tldr": "2 crisp sentences capturing the core problem and architectural solution.",
              "keyTakeaways": [
                "Concrete technical insight or benchmark 1",
                "Concrete technical insight or benchmark 2",
                "Concrete technical insight or benchmark 3"
              ],
              "tags": ["Tag1", "Tag2", "Tag3"]
            }
            Keep takeaways specific (mention numbers, algorithms, or trade-offs if present). Write strictly in clear, concise English.
            """;

    private final ArticleRepository articleRepository;
    private final ArticleVectorRepository vectorRepository;
    private final GeminiClient geminiClient;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public CurationSummaryResponse curateRawArticles() {
        List<Article> rawArticles = articleRepository.findAllByStatus(ArticleStatus.RAW);
        if (rawArticles.isEmpty()) {
            return new CurationSummaryResponse(0, 0, 0, 0, List.of());
        }

        List<Article> uniqueArticles = new ArrayList<>();
        int duplicatesCount = 0;

        for (Article article : rawArticles) {
            try {
                double maxSim;
                if (!vectorRepository.hasEmbedding(article.getId())) {
                    String textForEmbedding = article.getTitle() + "\n" + truncate(article.getRawContent(), 1200);
                    float[] vector = geminiClient.embedText(textForEmbedding);
                    vectorRepository.updateEmbedding(article.getId(), vector);
                }
                maxSim = vectorRepository.findMaxSimilarityByExistingEmbedding(article.getId()).orElse(0.0);

                if (maxSim >= SIMILARITY_THRESHOLD) {
                    article.setStatus(ArticleStatus.DUPLICATE);
                    articleRepository.save(article);
                    duplicatesCount++;
                    log.info("Marked duplicate [id={}, sim={}]: {}", article.getId(), String.format("%.3f", maxSim), article.getTitle());
                } else {
                    uniqueArticles.add(article);
                }
            } catch (Exception e) {
                article.setStatus(ArticleStatus.FAILED);
                articleRepository.save(article);
                log.error("Embedding failed for article [id={}]: {}", article.getId(), e.getMessage());
            }
        }

        if (uniqueArticles.isEmpty()) {
            return new CurationSummaryResponse(rawArticles.size(), duplicatesCount, 0, 0, List.of());
        }

        rankArticles(uniqueArticles);

        List<Article> sortedArticles = uniqueArticles.stream()
                .sorted(Comparator.comparing(Article::getRelevanceScore, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();

        List<Article> top5 = sortedArticles.stream().limit(TOP_N).toList();
        List<Article> remaining = sortedArticles.stream().skip(TOP_N).toList();

        List<CurationSummaryResponse.TopArticlePreview> previews = new ArrayList<>();
        for (Article article : top5) {
            try {
                String promptInput = "Title: " + article.getTitle() + "\n\nContent:\n" + article.getRawContent();
                String jsonResponse = geminiClient.generateJson(SUMMARIZE_SYSTEM_PROMPT, promptInput);
                ArticleSummaryResult result = objectMapper.readValue(jsonResponse, ArticleSummaryResult.class);

                article.setSummary(formatSummaryForStorage(result));
                article.setTags(String.join(", ", result.tags()));
                article.setStatus(ArticleStatus.SUMMARIZED);
                articleRepository.save(article);

                previews.add(new CurationSummaryResponse.TopArticlePreview(
                        article.getId(),
                        article.getTitle(),
                        article.getRelevanceScore(),
                        article.getScoreReason(),
                        article.getTags()
                ));
            } catch (Exception e) {
                log.error("Summarization failed for top article [id={}]: {}", article.getId(), e.getMessage());
                article.setStatus(ArticleStatus.FAILED);
                articleRepository.save(article);
            }
        }

        for (Article article : remaining) {
            article.setStatus(ArticleStatus.SUMMARIZED);
            articleRepository.save(article);
        }

        return new CurationSummaryResponse(
                rawArticles.size(),
                duplicatesCount,
                previews.size(),
                remaining.size(),
                previews
        );
    }

    private void rankArticles(List<Article> articles) {
        int batchSize = 15;
        for (int i = 0; i < articles.size(); i += batchSize) {
            List<Article> batch = articles.subList(i, Math.min(i + batchSize, articles.size()));
            scoreBatch(batch);
        }
    }

    private void scoreBatch(List<Article> batch) {
        try {
            List<Map<String,Object>> candidates = batch.stream()
                    .map(a -> Map.<String, Object>of(
                            "id", a.getId(),
                            "title", a.getTitle(),
                            "snippet", truncate(a.getRawContent(), 300)
                    ))
                    .toList();

            String payloadJson = objectMapper.writeValueAsString(candidates);
            String responseJson = geminiClient.generateJson(SCORING_SYSTEM_PROMPT, payloadJson);

            List<ArticleScoreResult> scores = objectMapper.readValue(
                    responseJson,
                    new TypeReference<>() {
                    }
            );

            Map<Long, ArticleScoreResult> scoreMap = scores.stream()
                    .collect(Collectors.toMap(ArticleScoreResult::id, Function.identity(), (a, b) -> a));

            for (Article article : batch) {
                ArticleScoreResult scoreResult = scoreMap.get(article.getId());
                if (scoreResult != null) {
                    article.setRelevanceScore(scoreResult.score());
                    article.setScoreReason(scoreResult.reason());
                } else {
                    article.setRelevanceScore(5.0);
                    article.setScoreReason("Default score");
                }
            }
        } catch (Exception e) {
            log.error("Batch scoring failed for chunk: {}", e.getMessage());
            batch.forEach(a -> {
                a.setRelevanceScore(5.0);
                a.setScoreReason("Fallback score due to scoring error");
            });
        }
    }

    private String formatSummaryForStorage(ArticleSummaryResult result) {
        String bullets = result.keyTakeaways().stream()
                .map(point -> "• " + point)
                .collect(Collectors.joining("\n"));
        return result.tldr() + "\n" + bullets;
    }

    private String truncate(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        return text.length() <= maxLength ? text : text.substring(0, maxLength);
    }
}