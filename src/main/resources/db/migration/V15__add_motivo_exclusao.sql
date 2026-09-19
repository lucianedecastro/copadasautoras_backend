-- V15: motivo de exclusão da autora (registro interno).
--
-- Aditiva e NULLABLE de propósito: as linhas que já estão em EXCLUIDA
-- ficam com motivo_exclusao = NULL, e o painel trata NULL como
-- encerramento neutro (jamais como "inadequação"). Risco baixo mesmo
-- com inscrições em andamento — não reescreve nem trava nada existente.
--
-- Guardado como texto (VARCHAR) para casar com @Enumerated(EnumType.STRING)
-- na entidade Autora, mesmo padrão de status_autora.
ALTER TABLE autora
    ADD COLUMN motivo_exclusao VARCHAR(20);
