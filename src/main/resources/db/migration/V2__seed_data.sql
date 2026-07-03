-- Seed data para desarrollo local
-- Contraseña para ambos usuarios: Password123
-- Hash BCrypt generado: $2a$10$8.Je5.O.T0Bv9gV1s82n/u5b3tD.Bq31F1QcO.Fk89K5w3e52O1uW

INSERT INTO usuarios (id, nombre, email, password_hash, telefono, departamento, municipio, nombre_finca, rol, activo, email_verificado, created_at, updated_at)
VALUES 
  (gen_random_uuid(), 'Juan Pérez', 'agricultor@cacao.com', '$2a$10$8.Je5.O.T0Bv9gV1s82n/u5b3tD.Bq31F1QcO.Fk89K5w3e52O1uW', '3101234567', 'Norte de Santander', 'Ocaña', 'Finca La Esmeralda', 'AGRICULTOR', true, true, now(), now())
ON CONFLICT (email) DO NOTHING;

INSERT INTO usuarios (id, nombre, email, password_hash, telefono, departamento, municipio, nombre_finca, rol, activo, email_verificado, created_at, updated_at)
VALUES 
  (gen_random_uuid(), 'Ing. Carlos Gómez', 'experto@cacao.com', '$2a$10$8.Je5.O.T0Bv9gV1s82n/u5b3tD.Bq31F1QcO.Fk89K5w3e52O1uW', '3207654321', 'Norte de Santander', 'Cúcuta', NULL, 'EXPERTO', true, true, now(), now())
ON CONFLICT (email) DO NOTHING;
