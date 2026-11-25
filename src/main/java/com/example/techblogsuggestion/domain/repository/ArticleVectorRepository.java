package com.example.techblogsuggestion.domain.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ArticleVectorRepository {

    private final JdbcClient jdbcClient;

    public void updateEmbedding(Long articleId, float[] vector) {
        String vectorLiteral = formatVector(vector);
        jdbcClient.sql("UPDATE articles SET embedding = CAST(:vec AS vector) WHERE id = :id")
                .param("vec", vectorLiteral)
                .param("id", articleId)
                .update();
    }

    public Optional<Double> findMaxSimilarityInLast48Hours(Long excludeArticleId, float[] targetVector) {
        String vectorLiteral = formatVector(targetVector);
        String sql = """
                SELECT MAX(1 - (embedding <=> CAST(:vec AS vector)))
                FROM articles
                WHERE id != :excludeId
                  AND embedding IS NOT NULL
                  AND status != 'DUPLICATE'
                  AND created_at > NOW() - INTERVAL '48 hours'
                """;

        return jdbcClient.sql(sql)
                .param("vec", vectorLiteral)
                .param("excludeId", excludeArticleId)
                .query(Double.class)
                .optional();
    }

    private String formatVector(float[] vector) {
        return Arrays.toString(vector).replace(" ", "");
    }

    public boolean hasEmbedding(Long articleId) {
        Boolean exists = jdbcClient.sql("SELECT embedding IS NOT NULL FROM articles WHERE id = :id")
                .param("id", articleId)
                .query(Boolean.class)
                .single();
        return Boolean.TRUE.equals(exists);
    }
    public Optional<Double> findMaxSimilarityByExistingEmbedding(Long articleId) {
        String sql = """
            SELECT MAX(1 - (a.embedding <=> target.embedding))
            FROM articles a, articles target
            WHERE target.id = :id
              AND a.id != :id
              AND a.embedding IS NOT NULL
              AND a.status != 'DUPLICATE'
              AND a.created_at > NOW() - INTERVAL '48 hours'
            """;

        return jdbcClient.sql(sql)
                .param("id", articleId)
                .query(Double.class)
                .optional();
    }
}