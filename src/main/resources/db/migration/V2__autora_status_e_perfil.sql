
-- =====================================================
-- V2__autora_status_e_perfil.sql
-- Evolução do perfil de autora
-- Copa das Autoras
-- =====================================================

-- 1. Nome de exibição da autora (nome usado na obra)
ALTER TABLE autora
ADD COLUMN nome_exibicao VARCHAR(255);

-- Inicializa com o nome já existente
UPDATE autora
SET nome_exibicao = nome
WHERE nome_exibicao IS NULL;

-- Torna obrigatório após migração dos dados
ALTER TABLE autora
ALTER COLUMN nome_exibicao SET NOT NULL;

-- 2. Site pessoal (opcional)
ALTER TABLE autora
ADD COLUMN site VARCHAR(500);

-- 3. Status da autora
ALTER TABLE autora
ADD COLUMN status_autora VARCHAR(50);

-- Inicializa autoras existentes como aprovadas
UPDATE autora
SET status_autora = 'APROVADA'
WHERE status_autora IS NULL;

-- Torna obrigatório
ALTER TABLE autora
ALTER COLUMN status_autora SET NOT NULL;

-- Constraint dos status permitidos
ALTER TABLE autora
ADD CONSTRAINT ck_autora_status
CHECK (
    status_autora IN (
        'PENDENTE',
        'APROVADA',
        'SUSPENSA',
        'EXCLUIDA'
    )
);

-- 4. Justificativa de exclusão (opcional)
ALTER TABLE autora
ADD COLUMN justificativa_exclusao TEXT;

