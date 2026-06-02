
-- =====================================================
-- V4__remove_email_autora.sql
-- Remove duplicidade de email da entidade Autora
-- Email passa a existir apenas em Usuario
-- =====================================================

ALTER TABLE autora
DROP COLUMN email;

