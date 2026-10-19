package com.cacaoscan.backend.service;

import com.cacaoscan.backend.dto.DiagnosticoDTOs.*;
import com.cacaoscan.backend.model.Diagnostico;
import com.cacaoscan.backend.model.Usuario;
import com.cacaoscan.backend.repository.DiagnosticoRepository;
import com.cacaoscan.backend.repository.UsuarioRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DiagnosticoService {

    private static final Set<String> TIPOS_PERMITIDOS = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/jpg"
    );

    @org.springframework.beans.factory.annotation.Value("${app.uploads.dir:uploads}")
    private String uploadsDir;

    @org.springframework.beans.factory.annotation.Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    private final DiagnosticoRepository diagnosticoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ObjectMapper objectMapper;
    private final MlServiceClient mlServiceClient;

    public DiagnosticoService(DiagnosticoRepository diagnosticoRepository,
                              UsuarioRepository usuarioRepository,
                              ObjectMapper objectMapper,
                              MlServiceClient mlServiceClient) {
        this.diagnosticoRepository = diagnosticoRepository;
        this.usuarioRepository = usuarioRepository;
        this.objectMapper = objectMapper;
        this.mlServiceClient = mlServiceClient;
    }

    /**
     * Procesa la imagen real enviándola al servicio de inferencia neuronal (EfficientNet-B0).
     * No genera resultados inventados: si el modelo falla o no responde, se lanza excepción explícita.
     */
    public DiagnosticoResponse procesarAnalisisIA(MultipartFile imagen, String userEmail) {
        // 1. Validar archivo
        if (imagen == null || imagen.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La imagen está vacía o no fue proporcionada.");
        }

        String contentType = imagen.getContentType();
        if (contentType == null || !TIPOS_PERMITIDOS.contains(contentType.toLowerCase())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Formato de imagen no soportado. Formatos válidos: JPEG, PNG o WebP.");
        }

        // 2. Enviar al microservicio ML
        String requestId = UUID.randomUUID().toString();
        MlPredictionResponse mlResponse = mlServiceClient.predict(imagen, requestId);

        if (mlResponse.getTopPrediction() == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "El clasificador no devolvió predicciones válidas.");
        }

        String topClass = mlResponse.getTopPrediction().getClassCode();
        double prob = mlResponse.getTopPrediction().getProbability();

        // 3. Mapear al DTO de CultivIA
        DiagnosticoResponse response = new DiagnosticoResponse();
        response.setId(UUID.randomUUID());
        response.setCodigoClase(topClass);
        response.setProbabilidad(prob);
        response.setConfianza(String.format(java.util.Locale.US, "%.1f%%", prob * 100));
        response.setModelVersion(mlResponse.getModelVersion());
        response.setConcluyente("ok".equalsIgnoreCase(mlResponse.getStatus()));

        // Guardar archivo físico en uploads/diagnosticos
        String fotoUrl;
        try {
            java.nio.file.Path uploadPath = java.nio.file.Paths.get(uploadsDir, "diagnosticos");
            java.nio.file.Files.createDirectories(uploadPath);

            String ext = ".jpg";
            if (contentType != null && contentType.contains("png")) ext = ".png";
            else if (contentType != null && contentType.contains("webp")) ext = ".webp";

            String fileName = requestId + ext;
            java.nio.file.Path filePath = uploadPath.resolve(fileName);
            java.nio.file.Files.write(filePath, imagen.getBytes());

            fotoUrl = baseUrl + "/uploads/diagnosticos/" + fileName;
        } catch (Exception e) {
            fotoUrl = imagen.getOriginalFilename() != null ? imagen.getOriginalFilename() : "captura_cacao.jpg";
        }
        response.setImagenUrl(fotoUrl);

        // 4. Mapear clase a enfermedad agronómica
        mapearClaseAEnfermedad(response, topClass);

        // 5. Asignar recomendaciones agronómicas aprobadas (contenido estático)
        response.setRecomendaciones(obtenerRecomendacionesPorClase(topClass));

        return response;
    }

    /**
     * Mapea el código de clase del modelo a los campos agronómicos.
     * Escoba de bruja y otras enfermedades fuera del alcance del modelo quedan excluidas.
     */
    private void mapearClaseAEnfermedad(DiagnosticoResponse response, String classCode) {
        if (classCode == null) classCode = "";

        switch (classCode) {
            case "healthy" -> {
                response.setTieneEnfermedad(false);
                response.setNombreEnfermedad("Mazorca Sana");
                response.setNombreCientifico("Theobroma cacao L.");
                response.setSeveridad("Nula");
            }
            case "monilia_m1" -> {
                response.setTieneEnfermedad(true);
                response.setNombreEnfermedad("Moniliasis — Etapa inicial");
                response.setNombreCientifico("Moniliophthora roreri");
                response.setSeveridad("Baja — abultamientos o deformaciones visibles");
            }
            case "monilia_m2" -> {
                response.setTieneEnfermedad(true);
                response.setNombreEnfermedad("Moniliasis — Etapa intermedia");
                response.setNombreCientifico("Moniliophthora roreri");
                response.setSeveridad("Media — mancha café/aceitosa");
            }
            case "monilia_m3" -> {
                response.setTieneEnfermedad(true);
                response.setNombreEnfermedad("Moniliasis — Etapa avanzada");
                response.setNombreCientifico("Moniliophthora roreri");
                response.setSeveridad("Alta — esporulación blanca visible");
            }
            default -> {
                response.setTieneEnfermedad(false);
                response.setNombreEnfermedad("Resultado no determinado");
                response.setNombreCientifico("Theobroma cacao L.");
                response.setSeveridad("No concluyente");
            }
        }
    }

    /**
     * Devuelve recomendaciones agronómicas revisadas según la clase detectada.
     * Contenido agronómico estático validado por expertos.
     */
    private List<RecomendacionItem> obtenerRecomendacionesPorClase(String classCode) {
        List<RecomendacionItem> recs = new ArrayList<>();
        if (classCode == null) classCode = "";

        switch (classCode) {
            case "healthy" -> {
                recs.add(new RecomendacionItem("1", "verde",
                        "Mantener monitoreo periódico",
                        "Inspeccione los frutos semanalmente durante la fase de llenado."));
                recs.add(new RecomendacionItem("2", "verde",
                        "Buenas prácticas de poda",
                        "Mantenga la aireación del dosel para prevenir acumulación de humedad excesiva."));
            }
            case "monilia_m1" -> {
                recs.add(new RecomendacionItem("1", "naranja",
                        "Monitoreo intensivo",
                        "Revise los frutos cercanos; las gibas o abultamientos pueden indicar infección temprana."));
                recs.add(new RecomendacionItem("2", "naranja",
                        "Considerar remoción preventiva",
                        "Si se confirma la infección temprana, retire el fruto antes de que aparezcan manchas."));
                recs.add(new RecomendacionItem("3", "cafe",
                        "Consultar agrónomo",
                        "Solicite evaluación técnica para confirmar y planificar medidas preventivas en el lote."));
            }
            case "monilia_m2" -> {
                recs.add(new RecomendacionItem("1", "rojo",
                        "Retirar fruto afectado",
                        "Corte el fruto con tijeras de podar desinfectadas para evitar transmitir esporas."));
                recs.add(new RecomendacionItem("2", "naranja",
                        "Enterrar bajo hojarasca",
                        "Entierre el fruto afectado bajo una capa gruesa de hojarasca para reducir la carga de inóculo."));
                recs.add(new RecomendacionItem("3", "cafe",
                        "Consultar agrónomo",
                        "Reciba asesoría técnica especializada para el manejo integrado de la moniliasis en su parcela."));
            }
            case "monilia_m3" -> {
                recs.add(new RecomendacionItem("1", "rojo",
                        "Retirar fruto inmediatamente",
                        "La masa blanca de esporas es altamente contagiosa con el viento. Retire y entierre de inmediato."));
                recs.add(new RecomendacionItem("2", "rojo",
                        "Inspeccionar frutos circundantes",
                        "Los frutos del mismo árbol y árboles vecinos tienen alto riesgo de infección por dispersión."));
                recs.add(new RecomendacionItem("3", "cafe",
                        "Consultar agrónomo urgente",
                        "Se requiere plan de choque: podas de ventilación, mejora de drenaje y posible biofungicida."));
            }
            default -> {
                recs.add(new RecomendacionItem("1", "cafe",
                        "Resultado no concluyente",
                        "Tome una nueva fotografía con buena luz natural, enfocando directamente la zona sospechosa."));
                recs.add(new RecomendacionItem("2", "cafe",
                        "Consultar a un experto",
                        "Comuníquese con el servicio de agrónomos en la aplicación para una revisión visual directa."));
            }
        }
        return recs;
    }

    /**
     * Guarda el diagnóstico en la base de datos de la finca con trazabilidad del modelo.
     */
    @Transactional
    public DiagnosticoResponse guardarDiagnostico(DiagnosticoResponse dto, String userEmail) {
        Usuario usuario = usuarioRepository.findByIdentificador(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        Diagnostico entity = new Diagnostico();
        entity.setUsuario(usuario);
        entity.setImagenUrl(dto.getImagenUrl() != null ? dto.getImagenUrl() : "");
        entity.setTieneEnfermedad(dto.isTieneEnfermedad());
        entity.setNombreEnfermedad(dto.getNombreEnfermedad());
        entity.setNombreCientifico(dto.getNombreCientifico());
        entity.setCodigoClase(dto.getCodigoClase());
        entity.setProbabilidad(dto.getProbabilidad());
        entity.setConfianza(dto.getConfianza());
        entity.setSeveridad(dto.getSeveridad());
        entity.setModelVersion(dto.getModelVersion());
        entity.setConcluyente(dto.isConcluyente());

        try {
            entity.setRecomendacionesJson(objectMapper.writeValueAsString(dto.getRecomendaciones()));
        } catch (JsonProcessingException e) {
            entity.setRecomendacionesJson("[]");
        }

        Diagnostico saved = diagnosticoRepository.save(entity);
        return mapToResponse(saved);
    }

    /**
     * Obtiene los diagnósticos del agricultor autenticado.
     */
    @Transactional(readOnly = true)
    public List<DiagnosticoResponse> obtenerHistorialUsuario(String userEmail) {
        Usuario usuario = usuarioRepository.findByIdentificador(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        return diagnosticoRepository.findByUsuarioOrderByCreatedAtDesc(usuario)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private DiagnosticoResponse mapToResponse(Diagnostico entity) {
        DiagnosticoResponse dto = new DiagnosticoResponse();
        dto.setId(entity.getId());
        dto.setImagenUrl(entity.getImagenUrl());
        dto.setTieneEnfermedad(entity.isTieneEnfermedad());
        dto.setNombreEnfermedad(entity.getNombreEnfermedad());
        dto.setNombreCientifico(entity.getNombreCientifico());
        dto.setCodigoClase(entity.getCodigoClase());
        dto.setProbabilidad(entity.getProbabilidad() != null ? entity.getProbabilidad() : 0.0);
        dto.setConfianza(entity.getConfianza());
        dto.setSeveridad(entity.getSeveridad());
        dto.setModelVersion(entity.getModelVersion());
        dto.setConcluyente(entity.isConcluyente() != null ? entity.isConcluyente() : true);
        dto.setCreatedAt(entity.getCreatedAt());

        if (entity.getRecomendacionesJson() != null) {
            try {
                List<RecomendacionItem> items = objectMapper.readValue(
                        entity.getRecomendacionesJson(),
                        new TypeReference<List<RecomendacionItem>>() {}
                );
                dto.setRecomendaciones(items);
            } catch (Exception e) {
                dto.setRecomendaciones(new ArrayList<>());
            }
        }
        return dto;
    }
}
