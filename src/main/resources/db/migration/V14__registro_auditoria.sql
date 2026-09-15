-- V14__registro_auditoria.sql
--
-- Log de auditoria semântico: registra DECISÕES com significado
-- (quem fez, o quê, antes -> depois, e por quê), não diffs de campo.
-- Serve autora, obra e banca com uma estrutura só.
--
-- Tabela nova: CREATE puro, nenhum dado existente é tocado.
-- O log é imutável por contrato — a aplicação só insere e lê,
-- nunca edita ou apaga (não há UPDATE/DELETE sobre ele no código).
--
-- entidade_id é um Long solto, SEM foreign key de propósito: o log
-- precisa sobreviver à exclusão da autora/obra que ele registra.

CREATE TABLE registro_auditoria (
    id              bigserial PRIMARY KEY,
    data_hora       timestamp     NOT NULL,
    origem          varchar(20)   NOT NULL,   -- ADMIN | BANCA | AUTORA | SISTEMA
    ator_id         bigint,                   -- id do Usuario que agiu (pode ser nulo)
    ator_nome       varchar(255),             -- snapshot do nome, verdadeiro mesmo se o usuário mudar/sair
    entidade        varchar(30)   NOT NULL,   -- AUTORA | SUBMISSAO
    entidade_id     bigint,                   -- id da autora/obra afetada
    acao            varchar(40)   NOT NULL,   -- APROVADA, SELECIONADA, CLASSIFICADA, ...
    valor_anterior  varchar(60),              -- estado antes (quando houver)
    valor_novo      varchar(60),              -- estado depois (quando houver)
    justificativa   text                      -- o porquê, quando existir
);

CREATE INDEX idx_auditoria_entidade  ON registro_auditoria (entidade, entidade_id);
CREATE INDEX idx_auditoria_data_hora ON registro_auditoria (data_hora);
