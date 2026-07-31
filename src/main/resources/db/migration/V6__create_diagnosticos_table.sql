-- Crear tabla de diagnósticos para guardar los resultados del análisis de IA
CREATE TABLE diagnosticos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id UUID NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    imagen_url TEXT NOT NULL,
    tiene_enfermedad BOOLEAN NOT NULL DEFAULT false,
    nombre_enfermedad VARCHAR(150) NOT NULL,
    nombre_cientifico VARCHAR(150),
    confianza VARCHAR(50) NOT NULL,
    severidad VARCHAR(50) NOT NULL,
    recomendaciones_json TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

-- Índice para consultar diagnósticos eficientemente por usuario
CREATE INDEX idx_diagnosticos_usuario_id ON diagnosticos(usuario_id);
CREATE INDEX idx_diagnosticos_created_at ON diagnosticos(created_at DESC);
