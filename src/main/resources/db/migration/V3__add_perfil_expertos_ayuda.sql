-- =============================================
-- V3: Nuevas tablas y columnas para módulo Perfil
-- =============================================

-- 1. Columnas nuevas en tabla usuarios
ALTER TABLE usuarios
  ADD COLUMN IF NOT EXISTS foto_perfil_url VARCHAR(500),
  ADD COLUMN IF NOT EXISTS hectareas DECIMAL(8,2),
  ADD COLUMN IF NOT EXISTS notificaciones_activas BOOLEAN NOT NULL DEFAULT true;

-- 2. Tabla de expertos
CREATE TABLE expertos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre VARCHAR(150) NOT NULL,
    especialidad VARCHAR(100) NOT NULL,
    descripcion TEXT,
    rating DECIMAL(2,1) DEFAULT 0.0,
    foto_perfil_url VARCHAR(500),
    disponible BOOLEAN NOT NULL DEFAULT false,
    tiempo_espera_minutos INTEGER,
    activo BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

-- 3. Tabla de chats
CREATE TABLE chats (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    agricultor_id UUID NOT NULL REFERENCES usuarios(id),
    experto_id UUID NOT NULL REFERENCES expertos(id),
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

-- 4. Tabla de preguntas frecuentes
CREATE TABLE ayuda_preguntas (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pregunta TEXT NOT NULL,
    respuesta TEXT NOT NULL,
    categoria VARCHAR(50),
    orden INTEGER NOT NULL DEFAULT 0,
    activo BOOLEAN NOT NULL DEFAULT true
);

-- 5. Tabla de categorías de ayuda
CREATE TABLE ayuda_categorias (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre VARCHAR(100) NOT NULL,
    icono VARCHAR(50) NOT NULL,
    color VARCHAR(7) NOT NULL,
    descripcion TEXT,
    orden INTEGER NOT NULL DEFAULT 0,
    activo BOOLEAN NOT NULL DEFAULT true
);

-- 6. Índices
CREATE INDEX idx_expertos_especialidad ON expertos(especialidad);
CREATE INDEX idx_expertos_disponible ON expertos(disponible);
CREATE INDEX idx_chats_agricultor ON chats(agricultor_id);
CREATE INDEX idx_chats_experto ON chats(experto_id);
CREATE INDEX idx_ayuda_preguntas_categoria ON ayuda_preguntas(categoria);
