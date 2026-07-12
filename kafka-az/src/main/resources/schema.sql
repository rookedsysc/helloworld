CREATE TABLE posts (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TYPE outbox_event_type AS ENUM ('POST_PUBLISHED');

CREATE TABLE outbox_events (
    id UUID PRIMARY KEY,
    aggregate_id BIGINT NOT NULL,
    event_type outbox_event_type NOT NULL,
    payload JSONB NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    published_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX outbox_events_pending_idx
    ON outbox_events (created_at)
    WHERE published_at IS NULL;
