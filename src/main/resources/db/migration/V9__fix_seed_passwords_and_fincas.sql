-- ============================================================================
-- V9: Corrección de contraseñas de prueba e inicialización de finca para agricultor
-- ============================================================================

-- 1. Actualizar contraseñas para los usuarios demo iniciales (Password123)
UPDATE usuarios 
SET password_hash = '$2a$10$qk0r7S4dD4kKi6Ez.Hqjj.IFJT6rEEp9AOHsdyeSAlVyIRUvLY2.W',
    updated_at = now()
WHERE email IN ('agricultor@cacao.com', 'experto@cacao.com');

-- 2. Asegurar que el usuario agricultor tenga una finca vinculada para el módulo geográfico
INSERT INTO fincas (id, usuario_id, nombre, departamento, municipio, vereda, area_total_hectareas, descripcion, activa, created_at, updated_at)
SELECT 
    gen_random_uuid(),
    u.id,
    COALESCE(u.nombre_finca, 'Finca La Esmeralda'),
    COALESCE(u.departamento, 'Norte de Santander'),
    COALESCE(u.municipio, 'Ocaña'),
    'Vereda El Vergel',
    COALESCE(u.hectareas, 5.5),
    'Finca cacaotera de demostración con clones certificados.',
    true,
    now(),
    now()
FROM usuarios u
WHERE u.email = 'agricultor@cacao.com'
  AND NOT EXISTS (
      SELECT 1 FROM fincas f WHERE f.usuario_id = u.id
  );
