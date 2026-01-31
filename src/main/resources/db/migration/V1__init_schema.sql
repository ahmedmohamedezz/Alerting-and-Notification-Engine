-- =========================================
-- Users Table
-- =========================================
CREATE TABLE users
(
    id             BIGSERIAL PRIMARY KEY,
    name           VARCHAR(100),
    email          VARCHAR(255) UNIQUE,
    email_verified BOOLEAN   DEFAULT FALSE,
    phone_number   VARCHAR(20) UNIQUE,
    phone_verified BOOLEAN   DEFAULT FALSE,
    role           VARCHAR(20) NOT NULL DEFAULT 'ROLE_USER',
    deleted        BOOLEAN   DEFAULT FALSE,
    created_at     TIMESTAMP DEFAULT NOW(),
    updated_at     TIMESTAMP DEFAULT NOW(),
    deleted_at     TIMESTAMP DEFAULT NULL,

    CONSTRAINT chk_users_role
        CHECK (role IN ('ROLE_USER', 'ROLE_ADMIN'));
);

-- =========================================
-- Categories Table
-- =========================================
CREATE TABLE categories
(
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL
);

-- =========================================
-- Channels Table
-- =========================================
CREATE TABLE channels
(
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL
);

-- =========================================
-- User Preferences Table
-- =========================================
CREATE TABLE users_preferences
(
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT REFERENCES users (id) ON DELETE CASCADE,
    channel_id  BIGINT REFERENCES channels (id) ON DELETE CASCADE,
    category_id BIGINT REFERENCES categories (id) ON DELETE CASCADE,
    UNIQUE (user_id, channel_id, category_id)
);

-- =========================================
-- Logs Table with BIG SERIAL + Cached Sequence
-- =========================================

-- First, create a sequence with caching
CREATE SEQUENCE logs_id_seq
    INCREMENT 1
    START 1
    CACHE 1000; -- preallocate 1000 IDs in memory for fast inserts

CREATE TABLE logs
(
    id          BIGINT PRIMARY KEY DEFAULT nextval('logs_id_seq'),
    user_id     BIGINT REFERENCES users (id) ON DELETE SET NULL,
    channel_id  BIGINT REFERENCES channels (id) ON DELETE SET NULL,
    category_id BIGINT REFERENCES categories (id) ON DELETE SET NULL,
    retry_count INT                DEFAULT 0,
    status      VARCHAR(20),
    response    JSONB, -- store structures APIs responses
    created_at  TIMESTAMP          DEFAULT NOW()
);

-- =========================================
-- Indexes for Logs Table
-- =========================================
CREATE INDEX idx_logs_user_id     ON logs(user_id);
CREATE INDEX idx_logs_channel_id  ON logs(channel_id);
CREATE INDEX idx_logs_category_id ON logs(category_id);
CREATE INDEX idx_logs_status      ON logs(status);
CREATE INDEX idx_logs_created_at  ON logs(created_at);
-- GIN index on response JSONB for fast JSON queries
CREATE INDEX idx_logs_response_gin ON logs USING GIN (response);
