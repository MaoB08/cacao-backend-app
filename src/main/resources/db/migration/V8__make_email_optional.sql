-- V8: Hacer email opcional y asegurar que telefono sea único
ALTER TABLE usuarios ALTER COLUMN email DROP NOT NULL;

-- Eliminar índice único previo si existe (para evitar conflictos)
ALTER TABLE usuarios DROP CONSTRAINT IF EXISTS usuarios_telefono_key;
ALTER TABLE usuarios ADD CONSTRAINT unique_telefono UNIQUE (telefono);
