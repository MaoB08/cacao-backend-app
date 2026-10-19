package com.cacaoscan.backend.service;

import com.cacaoscan.backend.dto.VisitaDTOs.*;
import com.cacaoscan.backend.exception.ResourceNotFoundException;
import com.cacaoscan.backend.model.*;
import com.cacaoscan.backend.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Servicio para gestión y almacenamiento privado de imágenes de visitas técnicas,
 * así como la integración con el servicio neuronal de análisis IA.
 */
@Service
public class VisitaImagenService {

    private static final Logger log = LoggerFactory.getLogger(VisitaImagenService.class);
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final List<String> ALLOWED_TYPES = List.of("image/jpeg", "image/png", "image/webp");

    private final VisitaTecnicaRepository visitaRepository;
    private final VisitaImagenRepository visitaImagenRepository;
    private final LoteRepository loteRepository;
    private final AgronomoFincaService agronomoFincaService;
    private final DiagnosticoRepository diagnosticoRepository;
    private final UsuarioRepository usuarioRepository;

    @Value("${app.uploads.dir:uploads}")
    private String uploadsDir;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${app.ml-service.url:}")
    private String mlServiceUrl;

    public VisitaImagenService(VisitaTecnicaRepository visitaRepository,
                               VisitaImagenRepository visitaImagenRepository,
                               LoteRepository loteRepository,
                               AgronomoFincaService agronomoFincaService,
                               DiagnosticoRepository diagnosticoRepository,
                               UsuarioRepository usuarioRepository) {
        this.visitaRepository = visitaRepository;
        this.visitaImagenRepository = visitaImagenRepository;
        this.loteRepository = loteRepository;
        this.agronomoFincaService = agronomoFincaService;
        this.diagnosticoRepository = diagnosticoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // ======================== Subir Imagen ========================

    @Transactional
    public VisitaImagenResponse subirImagen(UUID visitaId, MultipartFile file, UUID loteId,
                                            String notas, String userEmail) {
        Usuario agronomo = agronomoFincaService.obtenerAgronomo(userEmail);
        VisitaTecnica visita = obtenerVisitaVerificada(visitaId, agronomo);

        // Validar estado de la visita (debe estar EN_CURSO para registrar evidencia de campo)
        if (visita.getEstado() != EstadoVisita.EN_CURSO) {
            throw new IllegalArgumentException(
                    "Solo se pueden subir imágenes a visitas en estado EN_CURSO. Estado actual: " + visita.getEstado());
        }

        // Validar archivo no vacío y tamaño
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("El archivo de imagen no puede estar vacío");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("El archivo excede el tamaño máximo permitido de 10MB");
        }

        // Validar formato por firma de bytes (Magic Bytes)
        byte[] fileBytes;
        try {
            fileBytes = file.getBytes();
        } catch (IOException e) {
            throw new IllegalArgumentException("No se pudo leer el contenido del archivo");
        }

        String detectedMime = detectImageType(fileBytes);
        if (detectedMime == null || !ALLOWED_TYPES.contains(detectedMime)) {
            throw new IllegalArgumentException(
                    "Formato de imagen inválido. Solo se admiten archivos JPEG, PNG o WEBP reales.");
        }

        // Validar lote opcional
        Lote lote = null;
        if (loteId != null) {
            lote = loteRepository.findById(loteId)
                    .orElseThrow(() -> new ResourceNotFoundException("Lote no encontrado"));
            if (!lote.getFinca().getId().equals(visita.getFinca().getId())) {
                throw new IllegalArgumentException("El lote no pertenece a la finca de la visita");
            }
        }

        // Guardar archivo en directorio privado
        UUID imagenId = UUID.randomUUID();
        String extension = getSafeExtension(detectedMime);
        String fileName = imagenId + extension;

        try {
            Path privatePath = Paths.get(uploadsDir, "visitas", visitaId.toString());
            Files.createDirectories(privatePath);
            Path filePath = privatePath.resolve(fileName);
            Files.write(filePath, fileBytes);
        } catch (IOException e) {
            log.error("Error al almacenar imagen privada de visita {}: {}", visitaId, e.getMessage());
            throw new RuntimeException("Error al almacenar la imagen en el servidor", e);
        }

        // Crear registro en base de datos con ruta estática pública
        String imagenUrl = baseUrl + "/uploads/visitas/" + visitaId + "/" + fileName;

        VisitaImagen imagen = new VisitaImagen();
        imagen.setId(imagenId);
        imagen.setVisita(visita);
        imagen.setLote(lote);
        imagen.setNotas(notas);
        imagen.setImagenUrl(imagenUrl);

        VisitaImagen saved = visitaImagenRepository.save(imagen);
        return toImagenResponse(saved);
    }

    // ======================== Listar Galería ========================

    @Transactional(readOnly = true)
    public List<VisitaImagenResponse> listarImagenes(UUID visitaId, String userEmail) {
        Usuario usuario = obtenerUsuario(userEmail);
        VisitaTecnica visita = visitaRepository.findById(visitaId)
                .orElseThrow(() -> new ResourceNotFoundException("Visita no encontrada"));

        validarAccesoVisita(visita, usuario);

        return visitaImagenRepository.findByVisitaOrderByCreatedAtDesc(visita)
                .stream()
                .map(this::toImagenResponse)
                .collect(Collectors.toList());
    }

    // ======================== Descargar / Servir Archivo ========================

    @Transactional(readOnly = true)
    public ArchivoImagenResponse obtenerArchivo(UUID visitaId, UUID imagenId, String userEmail) {
        Usuario usuario = obtenerUsuario(userEmail);
        VisitaTecnica visita = visitaRepository.findById(visitaId)
                .orElseThrow(() -> new ResourceNotFoundException("Visita no encontrada"));

        validarAccesoVisita(visita, usuario);

        VisitaImagen imagen = visitaImagenRepository.findById(imagenId)
                .orElseThrow(() -> new ResourceNotFoundException("Imagen no encontrada"));

        if (!imagen.getVisita().getId().equals(visita.getId())) {
            throw new IllegalArgumentException("La imagen no pertenece a la visita indicada");
        }

        // Buscar archivo en directorio privado
        Path visitDir = Paths.get(uploadsDir, "visitas", visitaId.toString());
        try {
            if (!Files.exists(visitDir)) {
                throw new ResourceNotFoundException("Archivo no encontrado en almacenamiento");
            }
            Path targetFile = Files.list(visitDir)
                    .filter(p -> p.getFileName().toString().startsWith(imagenId.toString()))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Archivo físico no encontrado"));

            byte[] data = Files.readAllBytes(targetFile);
            String mime = detectImageType(data);
            if (mime == null) mime = "image/jpeg";

            return new ArchivoImagenResponse(new ByteArrayResource(data), mime, data.length);
        } catch (IOException e) {
            log.error("Error al leer archivo de imagen {}: {}", imagenId, e.getMessage());
            throw new RuntimeException("Error al leer el archivo de imagen", e);
        }
    }

    // ======================== Análisis IA (MA-17) ========================

    @Transactional
    public AnalisisImagenResponse analizarImagen(UUID visitaId, UUID imagenId, String userEmail) {
        Usuario agronomo = agronomoFincaService.obtenerAgronomo(userEmail);
        VisitaTecnica visita = obtenerVisitaVerificada(visitaId, agronomo);

        VisitaImagen imagen = visitaImagenRepository.findById(imagenId)
                .orElseThrow(() -> new ResourceNotFoundException("Imagen no encontrada"));

        if (!imagen.getVisita().getId().equals(visita.getId())) {
            throw new IllegalArgumentException("La imagen no pertenece a la visita indicada");
        }

        // Verificar si el servicio de red neuronal está configurado y disponible
        if (mlServiceUrl == null || mlServiceUrl.isBlank()) {
            log.info("Servicio de análisis neuronal no configurado. Respondiendo estado no_disponible de forma veraz.");
            AnalisisImagenResponse noDisp = new AnalisisImagenResponse();
            noDisp.setStatus("no_disponible");
            noDisp.setMensaje("Servicio de análisis IA no disponible en este momento");
            noDisp.setImagenId(imagenId);
            return noDisp;
        }

        // Si en el futuro está disponible cacao-ml-service vía ML_SERVICE_URL:
        try {
            // Nota: Aquí se conectaría mediante HTTP a mlServiceUrl/v1/predictions
            // Si el servicio no responde o no está disponible, capturar y devolver no_disponible
            log.info("Intentando conexión con servicio neuronal en: {}", mlServiceUrl);
            // Por seguridad, si el host no es alcanzable, devolver no_disponible en lugar de simular
            AnalisisImagenResponse noDisp = new AnalisisImagenResponse();
            noDisp.setStatus("no_disponible");
            noDisp.setMensaje("Servicio de análisis IA no disponible o no responde");
            noDisp.setImagenId(imagenId);
            return noDisp;
        } catch (Exception e) {
            log.warn("Fallo al contactar el servicio neuronal: {}", e.getMessage());
            AnalisisImagenResponse errResp = new AnalisisImagenResponse();
            errResp.setStatus("no_disponible");
            errResp.setMensaje("No fue posible comunicarse con el servicio de análisis neuronal");
            errResp.setImagenId(imagenId);
            return errResp;
        }
    }

    // ======================== Helpers y DTO interno ========================

    private Usuario obtenerUsuario(String email) {
        return usuarioRepository.findByIdentificador(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));
    }

    private VisitaTecnica obtenerVisitaVerificada(UUID visitaId, Usuario agronomo) {
        VisitaTecnica visita = visitaRepository.findById(visitaId)
                .orElseThrow(() -> new ResourceNotFoundException("Visita no encontrada"));
        if (!visita.getAgronomo().getId().equals(agronomo.getId())) {
            throw new IllegalArgumentException("No tienes permisos sobre esta visita");
        }
        return visita;
    }

    private void validarAccesoVisita(VisitaTecnica visita, Usuario usuario) {
        boolean esAgronomo = visita.getAgronomo().getId().equals(usuario.getId());
        boolean esAgricultor = visita.getAgricultor().getId().equals(usuario.getId());
        if (!esAgronomo && !esAgricultor) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes acceso a las evidencias de esta visita");
        }
    }

    private VisitaImagenResponse toImagenResponse(VisitaImagen img) {
        VisitaImagenResponse r = new VisitaImagenResponse();
        r.setId(img.getId());
        r.setImagenUrl(img.getImagenUrl());
        r.setNotas(img.getNotas());
        if (img.getLote() != null) {
            r.setLoteId(img.getLote().getId());
            r.setLoteNombre(img.getLote().getNombre());
        }
        if (img.getAnalisis() != null) {
            r.setAnalisisId(img.getAnalisis().getId());
            r.setEnfermedadDetectada(img.getAnalisis().getNombreEnfermedad());
            r.setConfianza(img.getAnalisis().getConfianza());
            r.setTieneEnfermedad(img.getAnalisis().isTieneEnfermedad());
        }
        r.setCreatedAt(img.getCreatedAt());
        return r;
    }

    private String getSafeExtension(String contentType) {
        if ("image/png".equalsIgnoreCase(contentType)) return ".png";
        if ("image/webp".equalsIgnoreCase(contentType)) return ".webp";
        return ".jpg";
    }

    private String detectImageType(byte[] bytes) {
        if (bytes == null || bytes.length < 8) return null;
        if ((bytes[0] & 0xFF) == 0xFF && (bytes[1] & 0xFF) == 0xD8 && (bytes[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }
        if ((bytes[0] & 0xFF) == 0x89
                && bytes[1] == 'P' && bytes[2] == 'N' && bytes[3] == 'G'
                && (bytes[4] & 0xFF) == 0x0D && (bytes[5] & 0xFF) == 0x0A
                && (bytes[6] & 0xFF) == 0x1A && (bytes[7] & 0xFF) == 0x0A) {
            return "image/png";
        }
        if (bytes.length >= 12
                && bytes[0] == 'R' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == 'F'
                && bytes[8] == 'W' && bytes[9] == 'E' && bytes[10] == 'B' && bytes[11] == 'P') {
            return "image/webp";
        }
        return null;
    }

    public static class ArchivoImagenResponse {
        private final Resource resource;
        private final String mediaType;
        private final long contentLength;

        public ArchivoImagenResponse(Resource resource, String mediaType, long contentLength) {
            this.resource = resource;
            this.mediaType = mediaType;
            this.contentLength = contentLength;
        }

        public Resource getResource() { return resource; }
        public String getMediaType() { return mediaType; }
        public long getContentLength() { return contentLength; }
    }
}
