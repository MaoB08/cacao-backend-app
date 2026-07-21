-- =============================================
-- V4: Datos de prueba para expertos y ayuda
-- =============================================

-- 1. Expertos de ejemplo
INSERT INTO expertos (id, nombre, especialidad, descripcion, rating, foto_perfil_url, disponible, tiempo_espera_minutos, activo, created_at)
VALUES
  (gen_random_uuid(), 'Ing. Mateo Valderrama', 'Plagas', 'Experto en control biológico de Monilia y manejo integrado de plagas en cacao fino de aroma. Más de 15 años de experiencia en la región del Catatumbo.', 4.9, NULL, true, NULL, true, now()),
  (gen_random_uuid(), 'Dra. Camila Restrepo', 'Suelos', 'Especialista en análisis de suelos tropicales y fertilización orgánica para cultivos de cacao. Investigadora asociada de Corpoica.', 4.7, NULL, false, 15, true, now()),
  (gen_random_uuid(), 'Ing. Roberto Mendoza', 'Cosecha', 'Consultor en técnicas de postcosecha, fermentación y secado de granos de cacao. Certificado en calidad de cacao fino.', 4.8, NULL, true, NULL, true, now());

-- 2. Preguntas frecuentes
INSERT INTO ayuda_preguntas (id, pregunta, respuesta, categoria, orden, activo)
VALUES
  (gen_random_uuid(), '¿Cómo registro una nueva cosecha de cacao?', 'Para registrar una nueva cosecha, dirígete a la pantalla principal y selecciona "Escanear Mazorca". Toma una foto clara de la mazorca y la app analizará automáticamente su estado. Los resultados se guardarán en tu historial de diagnósticos.', 'cosecha', 1, true),
  (gen_random_uuid(), 'La cámara no reconoce la mazorca, ¿qué hago?', 'Asegúrate de que la mazorca esté bien iluminada y centrada en el encuadre. Evita sombras fuertes y mantén una distancia de 15-30cm. Si el problema persiste, limpia la lente de la cámara y reinicia la aplicación.', 'camara', 2, true),
  (gen_random_uuid(), '¿Funciona la aplicación sin internet?', 'CacaoCare está diseñada para funcionar con conectividad limitada. Las funciones básicas de captura y registro están disponibles offline. Los datos se sincronizarán automáticamente cuando recuperes la conexión. Algunas funciones como el chat con expertos requieren conexión activa.', 'conectividad', 3, true);

-- 3. Categorías de ayuda
INSERT INTO ayuda_categorias (id, nombre, icono, color, descripcion, orden, activo)
VALUES
  (gen_random_uuid(), 'Uso de la Cámara', 'camera', '#1B4332', 'Aprende a identificar plagas y enfermedades con fotos inteligentes.', 1, true),
  (gen_random_uuid(), 'Gestión de Parcelas', 'grid', '#4CAF50', 'Organiza y monitorea tus parcelas de cacao eficientemente.', 2, true),
  (gen_random_uuid(), 'Conectividad', 'wifi', '#9E9E9E', 'Soluciones para usar la app con señal limitada en el campo.', 3, true),
  (gen_random_uuid(), 'Pagos y Ventas', 'dollar', '#F5E6CC', 'Gestiona tus ventas de cacao y registra pagos recibidos.', 4, true),
  (gen_random_uuid(), 'Seguridad', 'shield', '#FFE0B2', 'Protege tu cuenta y tus datos agrícolas de forma segura.', 5, true);
