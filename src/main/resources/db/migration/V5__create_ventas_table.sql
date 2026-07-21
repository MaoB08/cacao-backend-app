-- =============================================
-- V5: Tabla para almacenamiento de ventas de cacao
-- =============================================

CREATE TABLE IF NOT EXISTS ventas_cacao (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id UUID NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    cantidad DECIMAL(10,2) NOT NULL,
    unidad VARCHAR(20) NOT NULL,
    tipo_grano VARCHAR(50) NOT NULL,
    humedad VARCHAR(10) NOT NULL,
    total_estimado DECIMAL(12,2) NOT NULL,
    fecha TIMESTAMP NOT NULL DEFAULT now(),
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_ventas_usuario ON ventas_cacao(usuario_id);
