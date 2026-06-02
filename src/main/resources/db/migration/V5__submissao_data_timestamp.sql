ALTER TABLE submissao
ALTER COLUMN data_submissao
TYPE TIMESTAMP;

UPDATE submissao
SET data_submissao = NOW()
WHERE data_submissao IS NULL;

ALTER TABLE submissao
ALTER COLUMN data_submissao
SET NOT NULL;