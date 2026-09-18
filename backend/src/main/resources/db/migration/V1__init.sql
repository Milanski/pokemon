CREATE TABLE trainers (
    id              UUID PRIMARY KEY,
    username        VARCHAR(20) NOT NULL UNIQUE,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    registered_at   TIMESTAMP NOT NULL
);

CREATE TABLE collections (
    id              UUID PRIMARY KEY,
    trainer_id      UUID NOT NULL UNIQUE REFERENCES trainers (id) ON DELETE CASCADE
);

CREATE TABLE collection_entries (
    id              UUID PRIMARY KEY,
    collection_id   UUID NOT NULL REFERENCES collections (id) ON DELETE CASCADE,
    pokemon_id      INTEGER NOT NULL,
    pokemon_name    VARCHAR(100) NOT NULL,
    sprite_url      VARCHAR(500),
    caught_at       TIMESTAMP NOT NULL,
    CONSTRAINT uq_collection_pokemon UNIQUE (collection_id, pokemon_id)
);

CREATE INDEX idx_collection_entries_collection_id ON collection_entries (collection_id);
