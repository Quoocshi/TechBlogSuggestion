CREATE
EXTENSION IF NOT EXISTS vector;

CREATE TABLE feed_sources
(
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(255)  NOT NULL,
    rss_url         VARCHAR(2048) NOT NULL UNIQUE,
    category        VARCHAR(100)  NOT NULL,
    active          BOOLEAN       NOT NULL DEFAULT TRUE,
    last_fetched_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE articles
(
    id           BIGSERIAL PRIMARY KEY,
    source_id    BIGINT                   NOT NULL REFERENCES feed_sources (id) ON DELETE CASCADE,
    url          VARCHAR(2048)            NOT NULL UNIQUE,
    title        VARCHAR(500)             NOT NULL,
    raw_content  TEXT,
    summary      TEXT,
    tags         VARCHAR(500),
    embedding    vector(768),
    status       VARCHAR(50)              NOT NULL DEFAULT 'RAW',
    published_at TIMESTAMP WITH TIME ZONE,
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE daily_digests
(
    id               BIGSERIAL PRIMARY KEY,
    digest_date      DATE                     NOT NULL UNIQUE,
    compiled_content TEXT                     NOT NULL,
    article_count    INT                      NOT NULL,
    sent_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_articles_status ON articles (status);
CREATE INDEX idx_articles_created_at ON articles (created_at);