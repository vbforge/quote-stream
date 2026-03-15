-- V1__init_schema.sql
-- Quote Stream - Initial Schema

CREATE TABLE users (
    id          BIGSERIAL PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    email       VARCHAR(255) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE categories (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    owner_id    BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    UNIQUE (name, owner_id)
);

CREATE TABLE quotes (
    id          BIGSERIAL PRIMARY KEY,
    text        TEXT         NOT NULL,
    author      VARCHAR(255),
    is_public   BOOLEAN      NOT NULL DEFAULT TRUE,
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
    owner_id    BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category_id BIGINT       REFERENCES categories(id) ON DELETE SET NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- Stream settings per user (persisted for registered users)
CREATE TABLE stream_settings (
    id                BIGSERIAL PRIMARY KEY,
    user_id           BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    source_mode       VARCHAR(20) NOT NULL DEFAULT 'COMMUNITY', -- OWN / COMMUNITY / MIXED
    category_id       BIGINT REFERENCES categories(id) ON DELETE SET NULL,
    interval_seconds  INT NOT NULL DEFAULT 60,
    updated_at        TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Indexes for performance
CREATE INDEX idx_quotes_owner      ON quotes(owner_id);
CREATE INDEX idx_quotes_category   ON quotes(category_id);
CREATE INDEX idx_quotes_public     ON quotes(is_public, is_active);
CREATE INDEX idx_categories_owner  ON categories(owner_id);

-- Seed some default data for demo purposes
INSERT INTO users (username, email, password) VALUES
    ('demo', 'demo@quotestream.app', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewfyHPLtY0UiT2Oy');
    -- password: demo123

INSERT INTO categories (name, owner_id) VALUES
    ('Motivation', 1),
    ('Fun',        1),
    ('Philosophy', 1);

INSERT INTO quotes (text, author, is_public, is_active, owner_id, category_id) VALUES
    ('The only way to do great work is to love what you do.', 'Steve Jobs', TRUE, TRUE, 1, 1),
    ('Life is what happens when you''re busy making other plans.', 'John Lennon', TRUE, TRUE, 1, 2),
    ('We are what we repeatedly do. Excellence, then, is not an act, but a habit.', 'Aristotle', TRUE, TRUE, 1, 3),
    ('In the middle of every difficulty lies opportunity.', 'Albert Einstein', TRUE, TRUE, 1, 1),
    ('It does not matter how slowly you go as long as you do not stop.', 'Confucius', TRUE, TRUE, 1, 1),
    ('Two things are infinite: the universe and human stupidity. And I''m not sure about the universe.', 'Albert Einstein', TRUE, TRUE, 1, 2),
    ('The unexamined life is not worth living.', 'Socrates', TRUE, TRUE, 1, 3);

INSERT INTO stream_settings (user_id, source_mode, interval_seconds) VALUES (1, 'OWN', 60);
