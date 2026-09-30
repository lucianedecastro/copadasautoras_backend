-- V17: localização da autora (UF + cidade), para estatísticas agregadas.
--
-- Aditiva e NULLABLE: nenhuma autora existente muda de status nem quebra.
-- As colunas ficam NULL até a autora preencher no painel.

ALTER TABLE autora ADD COLUMN IF NOT EXISTS estado VARCHAR(2);
ALTER TABLE autora ADD COLUMN IF NOT EXISTS cidade VARCHAR(120);

-- Só aceita as 27 UFs (ou NULL). Garante que o relatório por estado
-- nunca receba "sp", "S. Paulo" ou lixo digitado por outro caminho.
ALTER TABLE autora
    ADD CONSTRAINT ck_autora_estado_uf
    CHECK (
        estado IS NULL OR estado IN (
            'AC','AL','AP','AM','BA','CE','DF','ES','GO','MA','MT','MS','MG',
            'PA','PB','PR','PE','PI','RJ','RN','RS','RO','RR','SC','SP','SE','TO'
        )
    );
