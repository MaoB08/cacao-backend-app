-- Actualizar mención de CacaoCare a CultivIA en preguntas frecuentes de ayuda
UPDATE ayuda_preguntas
SET respuesta = REPLACE(respuesta, 'CacaoCare', 'CultivIA')
WHERE respuesta LIKE '%CacaoCare%';
