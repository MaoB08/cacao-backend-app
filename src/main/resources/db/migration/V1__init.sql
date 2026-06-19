-- Crear tabla de usuarios
CREATE TABLE usuarios (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre VARCHAR(150) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    telefono VARCHAR(20),
    departamento VARCHAR(100),
    municipio VARCHAR(100),
    nombre_finca VARCHAR(150),
    rol VARCHAR(20) NOT NULL DEFAULT 'AGRICULTOR',
    activo BOOLEAN NOT NULL DEFAULT true,
    email_verificado BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

-- Crear tabla para almacenar refresh tokens hasheados
CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id UUID NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    token_hash VARCHAR(255) UNIQUE NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

-- Índices para búsquedas eficientes
CREATE INDEX idx_usuarios_email ON usuarios(email);
CREATE INDEX idx_refresh_tokens_token_hash ON refresh_tokens(token_hash);
