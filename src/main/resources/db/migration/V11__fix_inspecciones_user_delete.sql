-- Mantener consistente la política de borrado de datos geográficos propios.
-- La finca y el lote del usuario ya se eliminan en cascada; la referencia directa
-- de la inspección al mismo usuario también debe permitir la cascada.
ALTER TABLE inspecciones
    DROP CONSTRAINT IF EXISTS inspecciones_usuario_id_fkey;

ALTER TABLE inspecciones
    ADD CONSTRAINT fk_inspecciones_usuario
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE;
