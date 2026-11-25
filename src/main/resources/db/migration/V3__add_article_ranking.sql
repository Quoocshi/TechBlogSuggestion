ALTER TABLE articles
    ADD COLUMN relevance_score DOUBLE PRECISION DEFAULT 0.0,
    ADD COLUMN score_reason VARCHAR(500);

CREATE INDEX idx_articles_score ON articles(relevance_score DESC);

CREATE INDEX idx_articles_embedding_hnsw
    ON articles USING hnsw (embedding vector_cosine_ops);