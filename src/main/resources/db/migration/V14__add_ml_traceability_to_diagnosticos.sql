-- Agregar campos de trazabilidad del modelo de clasificación
ALTER TABLE diagnosticos
    ADD COLUMN IF NOT EXISTS codigo_clase VARCHAR(30),
    ADD COLUMN IF NOT EXISTS probabilidad DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS model_version VARCHAR(80),
    ADD COLUMN IF NOT EXISTS concluyente BOOLEAN DEFAULT true;

-- Índice para consultas por clase de enfermedad
CREATE INDEX IF NOT EXISTS idx_diagnosticos_codigo_clase
    ON diagnosticos(codigo_clase);

-- Índice para consultas por versión de modelo
CREATE INDEX IF NOT EXISTS idx_diagnosticos_model_version
    ON diagnosticos(model_version);

-- Comentarios para documentar el significado de los campos
COMMENT ON COLUMN diagnosticos.codigo_clase IS
    'Código estable del clasificador: healthy, monilia_m1, monilia_m2, monilia_m3';
COMMENT ON COLUMN diagnosticos.probabilidad IS
    'Probabilidad de la clase predicha [0.0, 1.0]. No es severidad.';
COMMENT ON COLUMN diagnosticos.model_version IS
    'Identificador de la versión del modelo que generó la predicción';
COMMENT ON COLUMN diagnosticos.concluyente IS
    'true si la confianza superó el umbral de abstención; false si fue "not_conclusive"';
