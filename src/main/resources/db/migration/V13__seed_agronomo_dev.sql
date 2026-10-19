-- ============================================================================
-- V13: Seed de datos de desarrollo para el Módulo del Agrónomo
-- ============================================================================

-- 1. Asegurar segundo agricultor de prueba (Password123)
INSERT INTO usuarios (id, nombre, email, password_hash, telefono, departamento, municipio, nombre_finca, rol, activo, email_verificado, created_at, updated_at)
VALUES 
  (gen_random_uuid(), 'Pedro Agricultor', 'pedro.cacao@cacao.com', '$2a$10$qk0r7S4dD4kKi6Ez.Hqjj.IFJT6rEEp9AOHsdyeSAlVyIRUvLY2.W', '3159876543', 'Santander', 'San Vicente de Chucurí', 'Finca Villa Cacao', 'AGRICULTOR', true, true, now(), now())
ON CONFLICT (email) DO NOTHING;

-- 2. Asegurar segunda finca para Pedro Agricultor
INSERT INTO fincas (id, usuario_id, nombre, departamento, municipio, vereda, area_total_hectareas, descripcion, activa, created_at, updated_at)
SELECT 
    gen_random_uuid(),
    u.id,
    'Finca Villa Cacao',
    'Santander',
    'San Vicente de Chucurí',
    'Vereda Cantarranas',
    8.0,
    'Finca modelo con cultivo tradicional de cacao híbrido y clones CCN-51.',
    true,
    now(),
    now()
FROM usuarios u
WHERE u.email = 'pedro.cacao@cacao.com'
  AND NOT EXISTS (
      SELECT 1 FROM fincas f WHERE f.usuario_id = u.id AND f.nombre = 'Finca Villa Cacao'
  );

-- 3. Crear lotes de prueba en las fincas si no existen
INSERT INTO lotes (id, finca_id, nombre, area_hectareas, variedad_cacao, ano_siembra, estado, geometria, created_at, updated_at)
SELECT 
    gen_random_uuid(),
    f.id,
    'Lote El Manantial',
    2.5,
    'CCN-51',
    2019,
    'ACTIVO',
    ST_GeomFromText('POLYGON((-73.415 6.880, -73.410 6.880, -73.410 6.885, -73.415 6.885, -73.415 6.880))', 4326),
    now(),
    now()
FROM fincas f
WHERE f.nombre = 'Finca La Esmeralda'
  AND NOT EXISTS (
      SELECT 1 FROM lotes l WHERE l.finca_id = f.id AND l.nombre = 'Lote El Manantial'
  );

INSERT INTO lotes (id, finca_id, nombre, area_hectareas, variedad_cacao, ano_siembra, estado, geometria, created_at, updated_at)
SELECT 
    gen_random_uuid(),
    f.id,
    'Lote Las Palmas',
    4.0,
    'Fedecacao San Vicente 41',
    2020,
    'ACTIVO',
    ST_GeomFromText('POLYGON((-73.420 6.890, -73.415 6.890, -73.415 6.895, -73.420 6.895, -73.420 6.890))', 4326),
    now(),
    now()
FROM fincas f
WHERE f.nombre = 'Finca Villa Cacao'
  AND NOT EXISTS (
      SELECT 1 FROM lotes l WHERE l.finca_id = f.id AND l.nombre = 'Lote Las Palmas'
  );

-- 4. Asignar las 2 fincas al agrónomo (Ing. Carlos Gómez - experto@cacao.com)
INSERT INTO agronomo_fincas (id, agronomo_id, finca_id, activa, created_at)
SELECT 
    gen_random_uuid(),
    a.id,
    f.id,
    true,
    now()
FROM usuarios a
CROSS JOIN fincas f
WHERE a.email = 'experto@cacao.com'
  AND f.nombre IN ('Finca La Esmeralda', 'Finca Villa Cacao')
  AND NOT EXISTS (
      SELECT 1 FROM agronomo_fincas af WHERE af.agronomo_id = a.id AND af.finca_id = f.id
  );

-- 5. Crear 3 visitas en distintos estados (COMPLETADA, EN_CURSO, PROGRAMADA)

-- Visita 1: COMPLETADA (hace 3 días en Finca La Esmeralda con Lote El Manantial)
INSERT INTO visitas_tecnicas (
    id, agronomo_id, finca_id, lote_id, agricultor_id,
    fecha_programada, duracion_estimada_min, motivo, notas_previas,
    fecha_inicio_real, fecha_fin_real, duracion_real_min,
    observaciones, recomendaciones, plantas_revisadas, plantas_enfermas,
    informe_final, estado, created_at, updated_at
)
SELECT 
    gen_random_uuid(),
    a.id,
    f.id,
    l.id,
    f.usuario_id,
    now() - INTERVAL '3 days',
    90,
    'Control de Moniliasis y revisión post-floración',
    'Verificar humedad relativa alta reportada por el agricultor la semana pasada.',
    now() - INTERVAL '3 days' + INTERVAL '8 hours',
    now() - INTERVAL '3 days' + INTERVAL '9 hours 35 minutes',
    95,
    'Se detectó presencia focalizada de Moniliophthora roreri en 3 árboles del sector oriental del lote.',
    '1. Podar de inmediato frutos enfermos y enterrarlos con cal viva.\n2. Mejorar ventilación podando chupones basales.\n3. Aplicar caldo bordelés en la próxima semana.',
    60,
    3,
    'Visita completada satisfactoriamente con diagnóstico oportuno.',
    'COMPLETADA',
    now() - INTERVAL '4 days',
    now() - INTERVAL '3 days'
FROM usuarios a
JOIN fincas f ON f.nombre = 'Finca La Esmeralda'
LEFT JOIN lotes l ON l.finca_id = f.id AND l.nombre = 'Lote El Manantial'
WHERE a.email = 'experto@cacao.com'
  AND NOT EXISTS (
      SELECT 1 FROM visitas_tecnicas v WHERE v.agronomo_id = a.id AND v.estado = 'COMPLETADA'
  );

-- Visita 2: EN_CURSO (Iniciada hoy en Finca Villa Cacao con Lote Las Palmas)
INSERT INTO visitas_tecnicas (
    id, agronomo_id, finca_id, lote_id, agricultor_id,
    fecha_programada, duracion_estimada_min, motivo, notas_previas,
    fecha_inicio_real, estado, created_at, updated_at
)
SELECT 
    gen_random_uuid(),
    a.id,
    f.id,
    l.id,
    f.usuario_id,
    now() - INTERVAL '1 hour',
    60,
    'Inspección sanitaria periódica y evaluación de mazorcas',
    'Llevar muestras para calibración de pruebas rápidas en campo.',
    now() - INTERVAL '30 minutes',
    'EN_CURSO',
    now() - INTERVAL '1 day',
    now()
FROM usuarios a
JOIN fincas f ON f.nombre = 'Finca Villa Cacao'
LEFT JOIN lotes l ON l.finca_id = f.id AND l.nombre = 'Lote Las Palmas'
WHERE a.email = 'experto@cacao.com'
  AND NOT EXISTS (
      SELECT 1 FROM visitas_tecnicas v WHERE v.agronomo_id = a.id AND v.estado = 'EN_CURSO'
  );

-- Visita 3: PROGRAMADA (Para dentro de 2 días en Finca La Esmeralda)
INSERT INTO visitas_tecnicas (
    id, agronomo_id, finca_id, lote_id, agricultor_id,
    fecha_programada, duracion_estimada_min, motivo, notas_previas,
    estado, created_at, updated_at
)
SELECT 
    gen_random_uuid(),
    a.id,
    f.id,
    l.id,
    f.usuario_id,
    now() + INTERVAL '2 days' + INTERVAL '3 hours',
    120,
    'Seguimiento a plan de fertilización orgánica y poda sanitaria',
    'Revisar si el productor adquirió los insumos recomendados.',
    'PROGRAMADA',
    now(),
    now()
FROM usuarios a
JOIN fincas f ON f.nombre = 'Finca La Esmeralda'
LEFT JOIN lotes l ON l.finca_id = f.id AND l.nombre = 'Lote El Manantial'
WHERE a.email = 'experto@cacao.com'
  AND NOT EXISTS (
      SELECT 1 FROM visitas_tecnicas v WHERE v.agronomo_id = a.id AND v.estado = 'PROGRAMADA'
  );

-- 6. Evidencia fotográfica de ejemplo para la visita completada
INSERT INTO visita_imagenes (id, visita_id, lote_id, imagen_url, notas, created_at)
SELECT 
    gen_random_uuid(),
    v.id,
    v.lote_id,
    'uploads/visitas/ejemplo_monilia_1.jpg',
    'Mazorca afectada con mancha marrón y micelio blanco incipiente.',
    now() - INTERVAL '3 days'
FROM visitas_tecnicas v
WHERE v.estado = 'COMPLETADA'
  AND NOT EXISTS (
      SELECT 1 FROM visita_imagenes vi WHERE vi.visita_id = v.id
  )
LIMIT 1;
