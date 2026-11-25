-- Reset existing embeddings to avoid casting error between 2048 and 768 dimensions
UPDATE articles SET embedding = NULL;

-- Revert column to 768 dimensions for Google text-embedding-004
ALTER TABLE articles ALTER COLUMN embedding TYPE vector(768);

-- Recreate HNSW index for vector(768) with cosine similarity (768 <= 2000 so HNSW is fully supported)
CREATE INDEX idx_articles_embedding_hnsw
    ON articles USING hnsw (embedding vector_cosine_ops);

