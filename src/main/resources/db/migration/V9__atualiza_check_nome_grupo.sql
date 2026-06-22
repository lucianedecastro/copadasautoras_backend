ALTER TABLE grupo_competicao DROP CONSTRAINT grupo_competicao_nome_grupo_check;

ALTER TABLE grupo_competicao ADD CONSTRAINT grupo_competicao_nome_grupo_check
    CHECK (nome_grupo IN (
        'A','B','C','D','E','F','G','H',
        'CONFRONTO_1','CONFRONTO_2','CONFRONTO_3','CONFRONTO_4',
        'CONFRONTO_5','CONFRONTO_6','CONFRONTO_7','CONFRONTO_8'
    ));