-- V16: votação popular — trecho liberado, consentimento e elegibilidade.
--
-- Aditiva de propósito: as três colunas novas em submissao nascem
-- com default seguro (false/NULL) e não afetam nenhuma obra já
-- cadastrada. elegivel_voto_popular é um flag de fato, setado uma
-- única vez quando a submissão atinge a semifinal — não é derivado
-- de fase_atual/status, então sobrevive a qualquer mudança posterior
-- desses campos (obra eliminada na semifinal continua elegível).
--
-- trecho_liberado tem teto de 5.600 caracteres (~4 laudas / 1 capítulo).
-- Validação de piso (4.200) fica no Bean Validation da entidade, não
-- no banco — trecho pode nascer NULL/vazio até a autora preencher.
ALTER TABLE submissao
    ADD COLUMN trecho_liberado VARCHAR(5600);

ALTER TABLE submissao
    ADD COLUMN autoriza_voto_popular BOOLEAN NOT NULL DEFAULT false;

ALTER TABLE submissao
    ADD COLUMN elegivel_voto_popular BOOLEAN NOT NULL DEFAULT false;

-- Tabela de votos. Um voto = um e-mail escolhendo UMA submissão
-- entre as elegíveis (semifinalistas). Segue o mesmo padrão de
-- token da migration V11 (reset de senha): token único, uso
-- vinculado à confirmação, sem uso repetido.
--
-- A constraint que impede voto duplicado é um índice único parcial
-- (email, apenas onde confirmado = true): isso permite reenviar o
-- e-mail de confirmação (nova linha pendente) sem bloquear, mas
-- garante que só um voto por e-mail se torna válido.
CREATE TABLE voto_popular (
    id BIGSERIAL PRIMARY KEY,
    submissao_id BIGINT NOT NULL REFERENCES submissao(id),
    email VARCHAR(255) NOT NULL,
    token VARCHAR(36) NOT NULL UNIQUE,
    confirmado BOOLEAN NOT NULL DEFAULT false,
    data_voto TIMESTAMP NOT NULL DEFAULT now(),
    data_confirmacao TIMESTAMP
);

CREATE UNIQUE INDEX idx_voto_popular_email_confirmado
    ON voto_popular (email)
    WHERE confirmado = true;