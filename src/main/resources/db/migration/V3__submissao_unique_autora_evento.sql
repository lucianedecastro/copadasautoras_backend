
-- =====================================================
-- V3__submissao_unique_autora_evento.sql
-- Regra: uma autora pode possuir apenas
-- uma submissão por evento
-- =====================================================

ALTER TABLE submissao
ADD CONSTRAINT uk_submissao_autora_evento
UNIQUE (autora_id, evento_id);

