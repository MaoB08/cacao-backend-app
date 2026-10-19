-- ============================================================
-- V12: Módulo del Agrónomo — tablas de asignación, visitas e imágenes
-- ============================================================

-- Asignación explícita agrónomo ↔ finca
CREATE TABLE agronomo_fincas (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    agronomo_id   UUID NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    finca_id      UUID NOT NULL REFERENCES fincas(id)   ON DELETE CASCADE,
    activa        BOOLEAN NOT NULL DEFAULT TRUE,
    asignado_por  UUID REFERENCES usuarios(id) ON DELETE SET NULL,
    created_at    TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (agronomo_id, finca_id)
);

CREATE INDEX idx_agronomo_fincas_agronomo ON agronomo_fincas(agronomo_id) WHERE activa = TRUE;
CREATE INDEX idx_agronomo_fincas_finca    ON agronomo_fincas(finca_id)    WHERE activa = TRUE;

-- Visitas técnicas del agrónomo
CREATE TABLE visitas_tecnicas (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    agronomo_id           UUID NOT NULL REFERENCES usuarios(id),
    finca_id              UUID NOT NULL REFERENCES fincas(id),
    lote_id               UUID REFERENCES lotes(id),
    agricultor_id         UUID NOT NULL REFERENCES usuarios(id),

    -- Agenda
    fecha_programada      TIMESTAMP NOT NULL,
    duracion_estimada_min INT,
    motivo                VARCHAR(500),
    notas_previas         TEXT,

    -- Ejecución
    fecha_inicio_real     TIMESTAMP,
    fecha_fin_real        TIMESTAMP,
    duracion_real_min     INT,

    -- Resultados
    observaciones         TEXT,
    recomendaciones       TEXT,
    plantas_revisadas     INT,
    plantas_enfermas      INT,
    informe_final         TEXT,

    -- Estado y ciclo de vida
    estado                VARCHAR(30) NOT NULL DEFAULT 'PROGRAMADA'
                          CHECK (estado IN ('PROGRAMADA','EN_CURSO','COMPLETADA','CANCELADA','REPROGRAMADA')),
    motivo_cancelacion    TEXT,

    -- Auditoría
    created_at            TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_visitas_agronomo_fecha ON visitas_tecnicas(agronomo_id, fecha_programada);
CREATE INDEX idx_visitas_finca_fecha    ON visitas_tecnicas(finca_id, fecha_programada);
CREATE INDEX idx_visitas_estado         ON visitas_tecnicas(estado);
CREATE INDEX idx_visitas_agricultor     ON visitas_tecnicas(agricultor_id);

-- Imágenes vinculadas a visitas
CREATE TABLE visita_imagenes (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    visita_id      UUID NOT NULL REFERENCES visitas_tecnicas(id) ON DELETE CASCADE,
    lote_id        UUID REFERENCES lotes(id),
    imagen_url     VARCHAR(500) NOT NULL,
    notas          TEXT,
    analisis_id    UUID REFERENCES diagnosticos(id) ON DELETE SET NULL,
    created_at     TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_visita_imagenes_visita ON visita_imagenes(visita_id);
