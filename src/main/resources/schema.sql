CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX IF NOT EXISTS idx_article_version_media_fileName_trgm 
ON article_version_media USING gin (file_name gin_trgm_ops);
