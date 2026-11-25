-- Rede Cidadã: esquema inicial no Postgres (substitui o SQLite).
-- Correções em relação ao SQLite (quadro "C · Postgres" no Miro):
--   * FK em todas as ligações com users, com ON DELETE pensado para a exclusão de conta:
--     CASCADE apaga o que é pessoal; SET NULL deixa o conteúdo público anônimo.
--   * BOOLEAN e TIMESTAMPTZ no lugar de 0/1 e milissegundos.
--   * Saem: cpf_masked, reels, campos de exibição gravados (time_ago, distance_meters,
--     distance_label, map_x/y_fraction, user_confirmed), contadores soltos e textos fixos
--     (calculados na consulta ou no código).
--   * Ids continuam em texto ("usr-...", "inc-...") porque o app já guarda esses valores.

CREATE TABLE users (
    id                TEXT PRIMARY KEY,
    name              TEXT NOT NULL,
    email             TEXT NOT NULL,
    password_hash     TEXT NOT NULL,
    neighborhood      TEXT NOT NULL,
    trust_score       INTEGER NOT NULL DEFAULT 0,
    anonymous_default BOOLEAN NOT NULL DEFAULT TRUE,
    geofence_radius   REAL NOT NULL DEFAULT 2.0,
    terms_accepted_at TIMESTAMPTZ,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);
-- E-mail único sem diferenciar maiúsculas.
CREATE UNIQUE INDEX users_email_lower_uq ON users (lower(email));

CREATE TABLE incidents (
    id                 TEXT PRIMARY KEY,
    reporter_id        TEXT REFERENCES users (id) ON DELETE SET NULL,
    category           TEXT NOT NULL,
    custom_badge_title TEXT,
    title              TEXT NOT NULL,
    description        TEXT NOT NULL,
    address            TEXT NOT NULL,
    neighborhood       TEXT NOT NULL,
    latitude           DOUBLE PRECISION NOT NULL,
    longitude          DOUBLE PRECISION NOT NULL,
    is_anonymous       BOOLEAN NOT NULL DEFAULT TRUE,
    status             TEXT NOT NULL,
    verified           BOOLEAN NOT NULL DEFAULT FALSE,
    hidden             BOOLEAN NOT NULL DEFAULT FALSE,
    reviewed           BOOLEAN NOT NULL DEFAULT FALSE,
    created_at         TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX incidents_created_at_idx ON incidents (created_at DESC);
CREATE INDEX incidents_reporter_idx ON incidents (reporter_id);

CREATE TABLE incident_media (
    id          TEXT PRIMARY KEY,
    incident_id TEXT NOT NULL REFERENCES incidents (id) ON DELETE CASCADE,
    uploader_id TEXT REFERENCES users (id) ON DELETE SET NULL,
    file_name   TEXT NOT NULL,
    mime_type   TEXT NOT NULL,
    size_bytes  BIGINT NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX incident_media_incident_idx ON incident_media (incident_id);

-- Comentário guarda o autor (não o nome em texto). Autor apagado = anônimo.
CREATE TABLE comments (
    id           TEXT PRIMARY KEY,
    incident_id  TEXT NOT NULL REFERENCES incidents (id) ON DELETE CASCADE,
    author_id    TEXT REFERENCES users (id) ON DELETE SET NULL,
    is_anonymous BOOLEAN NOT NULL DEFAULT TRUE,
    content      TEXT NOT NULL,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX comments_incident_idx ON comments (incident_id, created_at);

-- "Eu também vi": uma confirmação por pessoa.
CREATE TABLE incident_confirmations (
    incident_id TEXT NOT NULL REFERENCES incidents (id) ON DELETE CASCADE,
    user_id     TEXT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (incident_id, user_id)
);
CREATE INDEX incident_confirmations_user_idx ON incident_confirmations (user_id);

-- Denúncias: uma por pessoa por relato.
CREATE TABLE incident_reports (
    id               TEXT PRIMARY KEY,
    incident_id      TEXT NOT NULL REFERENCES incidents (id) ON DELETE CASCADE,
    reporter_user_id TEXT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    reason           TEXT NOT NULL CHECK (reason IN ('FALSO', 'OFENSIVO', 'DADOS_PESSOAIS', 'VIOLENCIA', 'SPAM', 'OUTRO')),
    details          TEXT NOT NULL DEFAULT '',
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (incident_id, reporter_user_id)
);
CREATE INDEX incident_reports_user_idx ON incident_reports (reporter_user_id);

CREATE TABLE user_blocks (
    blocker_id TEXT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    blocked_id TEXT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (blocker_id, blocked_id)
);
CREATE INDEX user_blocks_blocked_idx ON user_blocks (blocked_id);

-- Contatos de emergência (o guardião não tem conta no app).
CREATE TABLE guardians (
    id         TEXT PRIMARY KEY,
    user_id    TEXT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    name       TEXT NOT NULL,
    relation   TEXT NOT NULL,
    phone      TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX guardians_user_idx ON guardians (user_id, created_at);

-- O SOS não aciona nenhum órgão: só registra e conta os guardiões avisados pela própria pessoa.
CREATE TABLE sos_alerts (
    protocol_id             TEXT PRIMARY KEY,
    user_id                 TEXT REFERENCES users (id) ON DELETE CASCADE,
    neighborhood            TEXT NOT NULL,
    latitude                DOUBLE PRECISION,
    longitude               DOUBLE PRECISION,
    emergency_type          TEXT NOT NULL,
    trigger_source          TEXT NOT NULL,
    status                  TEXT NOT NULL,
    guardians_alerted_count INTEGER NOT NULL DEFAULT 0,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX sos_alerts_user_idx ON sos_alerts (user_id);

-- Só o hash SHA-256 do refresh token é guardado.
CREATE TABLE refresh_tokens (
    id         TEXT PRIMARY KEY,
    user_id    TEXT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    token_hash TEXT NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked    BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX refresh_tokens_user_idx ON refresh_tokens (user_id);
