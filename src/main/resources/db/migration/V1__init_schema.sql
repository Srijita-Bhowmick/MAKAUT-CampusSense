-- V1__init_schema.sql
-- MAKAUT CampusSense Database Initialization

-- Enable pgvector extension
CREATE EXTENSION IF NOT EXISTS vector;

-- Colleges table
CREATE TABLE IF NOT EXISTS colleges (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    location VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Academic Resources table
CREATE TABLE IF NOT EXISTS academic_resources (
    id BIGSERIAL PRIMARY KEY,
    stream VARCHAR(50) NOT NULL,
    semester INT NOT NULL CHECK (semester BETWEEN 1 AND 8),
    subject_code VARCHAR(50) NOT NULL,
    subject_name VARCHAR(255) NOT NULL,
    resource_type VARCHAR(50) NOT NULL,
    year INT,
    file_url VARCHAR(1024),
    raw_text TEXT,
    metadata JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Document Embeddings with pgvector (1536 dimensions)
CREATE TABLE IF NOT EXISTS document_embeddings (
    id BIGSERIAL PRIMARY KEY,
    resource_id BIGINT NOT NULL REFERENCES academic_resources(id) ON DELETE CASCADE,
    chunk_index INT NOT NULL,
    content TEXT NOT NULL,
    embedding vector(1536) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Placement Statistics table
CREATE TABLE IF NOT EXISTS placement_stats (
    id BIGSERIAL PRIMARY KEY,
    college_id BIGINT NOT NULL REFERENCES colleges(id) ON DELETE CASCADE,
    batch_year INT NOT NULL,
    company_name VARCHAR(255) NOT NULL,
    highest_ctc NUMERIC(10, 2),
    average_ctc NUMERIC(10, 2),
    total_offers INT,
    source_url VARCHAR(1024),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Chat Sessions
CREATE TABLE IF NOT EXISTS chat_sessions (
    id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Chat History for Session-managed RAG
CREATE TABLE IF NOT EXISTS chat_history (
    id BIGSERIAL PRIMARY KEY,
    session_id UUID NOT NULL,
    role VARCHAR(20) NOT NULL,
    message TEXT NOT NULL,
    context_sources JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_resources_filter ON academic_resources(stream, semester, subject_code);
CREATE INDEX IF NOT EXISTS idx_resources_type ON academic_resources(resource_type);
CREATE INDEX IF NOT EXISTS idx_placement_college_year ON placement_stats(college_id, batch_year);
CREATE INDEX IF NOT EXISTS idx_chat_session ON chat_history(session_id);

-- HNSW Vector Index for sub-200ms Top-K Cosine Similarity Retrieval
CREATE INDEX IF NOT EXISTS idx_doc_embeddings_hnsw 
    ON document_embeddings USING hnsw (embedding vector_cosine_ops);
