-- V13__autora_data_cadastro_e_atualizacao.sql
--
-- Adiciona à autora o carimbo de cadastro e o de última atualização.
--
-- As duas colunas nascem NULLABLE de propósito. As autoras já
-- existentes (cadastradas antes deste registro de data) ficam com
-- data_cadastro NULL — "legado", sem data conhecida. Não há backfill:
-- o banco não guarda, em lugar nenhum, a data real de cadastro dessas
-- autoras (o Usuario não tem timestamp e apenas 1 das 49 tem submissão),
-- portanto qualquer preenchimento seria dado inventado.
--
-- Daqui em diante:
--   data_cadastro    -> preenchida uma vez, no cadastro (@CreationTimestamp)
--   data_atualizacao -> preenchida no cadastro e a cada alteração (@UpdateTimestamp)
--
-- Só estrutura, nenhum UPDATE — as linhas existentes não são tocadas.

ALTER TABLE autora
    ADD COLUMN data_cadastro    timestamp,
    ADD COLUMN data_atualizacao timestamp;
