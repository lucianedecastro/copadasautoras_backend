-- V18: encerramento da votação popular e selo "Escolha do Público".
--
-- Aditiva de propósito: colunas novas nascem com default seguro
-- (false/NULL) e não alteram nenhum dado existente.
--
-- competicao.votacao_popular_encerrada
--   false enquanto a votação aceita votos. O admin encerra (e pode
--   reabrir) pelo painel; ao encerrar, não entram votos novos e os
--   pendentes deixam de poder ser confirmados.
--
-- submissao.selo_escolha_publico
--   true na obra vencedora do Escolha do Público. Removido se a
--   votação for reaberta.
--
-- registro_auditoria não precisa de mudança: acao é varchar(40) sem
-- CHECK, então as ações novas (VOTACAO_POPULAR_ENCERRADA,
-- SELO_ESCOLHA_PUBLICO, VOTACAO_POPULAR_REABERTA) entram direto.
ALTER TABLE competicao
    ADD COLUMN votacao_popular_encerrada BOOLEAN NOT NULL DEFAULT false;

ALTER TABLE competicao
    ADD COLUMN votacao_popular_encerrada_em TIMESTAMP;

ALTER TABLE submissao
    ADD COLUMN selo_escolha_publico BOOLEAN NOT NULL DEFAULT false;
