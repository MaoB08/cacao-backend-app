-- ============================================================================
-- V7: Módulo Geográfico - Fincas, Lotes, Inspecciones, Detecciones Georeferenciadas
-- Habilita PostGIS y crea las tablas necesarias para el manejo espacial
-- de parcelas agrícolas y detecciones de enfermedades del cacao.
-- ============================================================================

-- 1. Habilitar extensión PostGIS
CREATE EXTENSION IF NOT EXISTS postgis;

-- ============================================================================
-- 2. Tabla: fincas
-- Representa la finca agrícola del productor de cacao.
-- Un usuario puede tener múltiples fincas.
-- ============================================================================
CREATE TABLE fincas (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id UUID NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    nombre VARCHAR(200) NOT NULL,
    departamento VARCHAR(100),
    municipio VARCHAR(100),
    vereda VARCHAR(150),
    area_total_hectareas DECIMAL(10, 4),
    descripcion TEXT,
    activa BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_fincas_usuario_id ON fincas(usuario_id);

-- ============================================================================
-- 3. Tabla: lotes
-- Representa un lote/parcela dentro de una finca.
-- La geometría se almacena como Polygon SRID 4326 (WGS 84).
-- El área se calcula automáticamente desde la geometría al insertar/actualizar.
-- ============================================================================
CREATE TABLE lotes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    finca_id UUID NOT NULL REFERENCES fincas(id) ON DELETE CASCADE,
    nombre VARCHAR(200) NOT NULL,
    variedad_cacao VARCHAR(100),
    ano_siembra INTEGER,
    numero_plantas INTEGER,
    geometria GEOMETRY(Polygon, 4326) NOT NULL,
    area_hectareas DECIMAL(10, 4),
    perimetro_metros DECIMAL(12, 2),
    color_hex VARCHAR(9) NOT NULL DEFAULT '#4CAF50',
    estado VARCHAR(30) NOT NULL DEFAULT 'ACTIVO',
    notas TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

-- Índice espacial GiST para consultas geoespaciales eficientes sobre lotes
CREATE INDEX idx_lotes_geometria ON lotes USING GIST (geometria);
CREATE INDEX idx_lotes_finca_id ON lotes(finca_id);
CREATE INDEX idx_lotes_estado ON lotes(estado);

-- ============================================================================
-- 4. Tabla: inspecciones
-- Cada vez que un agricultor recorre un lote para inspeccionar plantas.
-- ============================================================================
CREATE TABLE inspecciones (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    lote_id UUID NOT NULL REFERENCES lotes(id) ON DELETE CASCADE,
    usuario_id UUID NOT NULL REFERENCES usuarios(id) ON DELETE SET NULL,
    fecha_inspeccion TIMESTAMP NOT NULL DEFAULT now(),
    plantas_inspeccionadas INTEGER NOT NULL DEFAULT 0,
    plantas_enfermas INTEGER NOT NULL DEFAULT 0,
    total_enfermedades INTEGER NOT NULL DEFAULT 0,
    observaciones TEXT,
    estado VARCHAR(30) NOT NULL DEFAULT 'COMPLETADA',
    duracion_minutos INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_inspecciones_lote_id ON inspecciones(lote_id);
CREATE INDEX idx_inspecciones_usuario_id ON inspecciones(usuario_id);
CREATE INDEX idx_inspecciones_fecha ON inspecciones(fecha_inspeccion DESC);

-- ============================================================================
-- 5. Tabla: detecciones_georeferenciadas
-- Cada detección de enfermedad de la IA con su ubicación exacta (Point).
-- Se vincula automáticamente al lote mediante ST_Contains al insertar.
-- ============================================================================
CREATE TABLE detecciones_georeferenciadas (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    lote_id UUID REFERENCES lotes(id) ON DELETE SET NULL,
    inspeccion_id UUID REFERENCES inspecciones(id) ON DELETE SET NULL,
    usuario_id UUID NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    enfermedad VARCHAR(150) NOT NULL,
    porcentaje_confianza DECIMAL(5, 2) NOT NULL,
    severidad VARCHAR(50),
    imagen_url TEXT,
    ubicacion GEOMETRY(Point, 4326),
    observaciones TEXT,
    fecha_deteccion TIMESTAMP NOT NULL DEFAULT now(),
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

-- Índice espacial GiST para consultas geoespaciales sobre detecciones
CREATE INDEX idx_detecciones_geo_ubicacion ON detecciones_georeferenciadas USING GIST (ubicacion);
CREATE INDEX idx_detecciones_geo_lote_id ON detecciones_georeferenciadas(lote_id);
CREATE INDEX idx_detecciones_geo_inspeccion_id ON detecciones_georeferenciadas(inspeccion_id);
CREATE INDEX idx_detecciones_geo_usuario_id ON detecciones_georeferenciadas(usuario_id);
CREATE INDEX idx_detecciones_geo_enfermedad ON detecciones_georeferenciadas(enfermedad);
CREATE INDEX idx_detecciones_geo_fecha ON detecciones_georeferenciadas(fecha_deteccion DESC);
