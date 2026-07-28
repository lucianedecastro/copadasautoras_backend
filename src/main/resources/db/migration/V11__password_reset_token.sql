CREATE TABLE password_reset_token (
    id          BIGSERIAL PRIMARY KEY,
    token       VARCHAR(255) NOT NULL UNIQUE,
    usuario_id  BIGINT NOT NULL,
    expira_em   TIMESTAMP NOT NULL,
    usado       BOOLEAN NOT NULL DEFAULT FALSE,
    criado_em   TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_reset_token_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuario (id)
        ON DELETE CASCADE
);

CREATE INDEX idx_reset_token_token ON password_reset_token (token);