INSERT INTO feed_sources (id, name, rss_url, category, active, last_fetched_at, source_type)
VALUES
    (1, 'Uber Engineering', 'https://www.uber.com/us/en/blog/engineering/', 'TECH', true, '2026-11-18 13:46:49.619757+00', 'UBER_HTML'),
    (2, 'Grab Engineering', 'https://engineering.grab.com/feed.xml', 'TECH', true, '2026-11-18 13:46:50.499873+00', 'RSS'),
    (3, 'InfoQ', 'https://feed.infoq.com/', 'ARCHITECTURE', true, '2026-11-18 13:46:58.854084+00', 'RSS')
ON CONFLICT (rss_url) DO UPDATE SET
    name = EXCLUDED.name,
    category = EXCLUDED.category,
    active = EXCLUDED.active,
    last_fetched_at = EXCLUDED.last_fetched_at,
    source_type = EXCLUDED.source_type;

-- Synchronize sequence with the max id to prevent duplicate key errors on future inserts
SELECT setval('feed_sources_id_seq', COALESCE((SELECT MAX(id) FROM feed_sources), 1));

