-- =====================================================
-- V12 — Lance a Lance (newsroom da Copa das Autoras)
--
-- Arquivo permanente de clipping, apoios/patrocínios,
-- embaixadoras e temas. Cada entrada é um "lance"; a
-- flag "golaco" marca os destaques (selo dourado).
--
-- Mídias ficam em tabela filha (lance_midia), com origem
-- UPLOAD (Cloudinary) ou EMBED (rádio/TV/YouTube).
-- =====================================================

CREATE TABLE lance (
    id                  BIGSERIAL PRIMARY KEY,
    titulo              VARCHAR(255) NOT NULL,
    resumo              TEXT,
    categoria           VARCHAR(30)  NOT NULL,
    golaco              BOOLEAN      NOT NULL DEFAULT FALSE,
    veiculo             VARCHAR(255),
    link_externo        TEXT,
    status              VARCHAR(20)  NOT NULL DEFAULT 'RASCUNHO',
    data_acontecimento  DATE         NOT NULL,
    publicar_em         TIMESTAMP,
    slug                VARCHAR(255) NOT NULL UNIQUE,
    data_criacao        TIMESTAMP    NOT NULL DEFAULT NOW(),
    data_atualizacao    TIMESTAMP
);

-- Índices para a timeline pública e os filtros do admin.
CREATE INDEX idx_lance_status             ON lance (status);
CREATE INDEX idx_lance_categoria          ON lance (categoria);
CREATE INDEX idx_lance_data_acontecimento ON lance (data_acontecimento);

CREATE TABLE lance_midia (
    id        BIGSERIAL PRIMARY KEY,
    lance_id  BIGINT       NOT NULL,
    tipo      VARCHAR(20)  NOT NULL,
    origem    VARCHAR(20)  NOT NULL,
    url       TEXT         NOT NULL,
    legenda   VARCHAR(500),
    ordem     INTEGER,
    CONSTRAINT fk_lance_midia_lance
        FOREIGN KEY (lance_id)
        REFERENCES lance (id)
        ON DELETE CASCADE
);

CREATE INDEX idx_lance_midia_lance ON lance_midia (lance_id);
