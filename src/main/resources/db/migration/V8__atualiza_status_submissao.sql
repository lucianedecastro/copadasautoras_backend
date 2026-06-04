ALTER TABLE submissao
DROP CONSTRAINT IF EXISTS submissao_status_check;

ALTER TABLE submissao
ADD CONSTRAINT submissao_status_check
CHECK (
    status IN (
        'SUBMETIDA',
        'NAO_SELECIONADA',
        'EM_COMPETICAO',
        'CLASSIFICADA',
        'ELIMINADA',
        'CAMPEA'
    )
);