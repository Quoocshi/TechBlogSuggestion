-- Drop the index referencing the old vector dimension
DROP INDEX IF EXISTS idx_articles_embedding_hnsw;

-- Reset existing embeddings to avoid casting error between 768 and 2048 dimensions
UPDATE articles SET embedding = NULL;

-- Alter column to 2048 dimensions for NVIDIA Nemotron 3.5 Embed.
ALTER TABLE articles ALTER COLUMN embedding TYPE vector(2048);
