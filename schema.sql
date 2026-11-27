-- Execute no SQL Editor do Supabase antes do deploy. Pode executar novamente sem apagar dados.
BEGIN;
CREATE SCHEMA IF NOT EXISTS gestao_privado;
CREATE TABLE IF NOT EXISTS gestao_privado.estado (
 id integer PRIMARY KEY CHECK (id = 1),
 revisao bigint NOT NULL DEFAULT 0 CHECK (revisao >= 0),
 dados jsonb NOT NULL CHECK (jsonb_typeof(dados) = 'object'),
 atualizado_em timestamptz NOT NULL DEFAULT now()
);
ALTER TABLE gestao_privado.estado ENABLE ROW LEVEL SECURITY;
REVOKE ALL ON SCHEMA gestao_privado FROM PUBLIC, anon, authenticated;
REVOKE ALL ON TABLE gestao_privado.estado FROM PUBLIC, anon, authenticated;
COMMIT;
-- O Java inicializa a linha de demonstração somente se ela não existir.
