package com.cacaoscan.backend.service;

import com.cacaoscan.backend.dto.PerfilResponse;
import com.cacaoscan.backend.dto.PerfilUpdateRequest;
import com.cacaoscan.backend.model.Usuario;
import com.cacaoscan.backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class PerfilService {

    private final UsuarioRepository usuarioRepository;

    @Value("${app.uploads.dir:uploads}")
    private String uploadsDir;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final List<String> ALLOWED_TYPES = List.of("image/jpeg", "image/png", "image/webp");

    public PerfilService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Obtiene el perfil completo del usuario autenticado.
     */
    public PerfilResponse getPerfil(String email) {
        Usuario usuario = usuarioRepository.findByIdentificador(email)
                .orElseThrow(() -> new IllegalArgumentException("usuario_no_encontrado"));

        PerfilResponse response = new PerfilResponse();
        response.setId(usuario.getId());
        response.setNombre(usuario.getNombre());
        response.setEmail(usuario.getEmail());
        response.setTelefono(usuario.getTelefono());
        response.setFotoPerfil(usuario.getFotoPerfilUrl());
        response.setNotificacionesActivas(usuario.isNotificacionesActivas());
        response.setVersionApp("1.0.0");

        // Finca info
        PerfilResponse.FincaInfo finca = new PerfilResponse.FincaInfo(
                usuario.getNombreFinca(),
                usuario.getDepartamento(),
                usuario.getMunicipio(),
                usuario.getHectareas()
        );
        response.setFinca(finca);

        // TODO: Calcular estadísticas reales cuando la tabla de diagnósticos exista
        // Por ahora devolver datos mock
        PerfilResponse.EstadisticasInfo stats = new PerfilResponse.EstadisticasInfo(0, 0, "Sin datos");
        response.setEstadisticas(stats);

        return response;
    }

    /**
     * Actualiza parcialmente el perfil del usuario.
     */
    @Transactional
    public PerfilResponse updatePerfil(String email, PerfilUpdateRequest request) {
        Usuario usuario = usuarioRepository.findByIdentificador(email)
                .orElseThrow(() -> new IllegalArgumentException("usuario_no_encontrado"));

        if (request.getNombre() != null) {
            usuario.setNombre(request.getNombre());
        }
        if (request.getTelefono() != null) {
            usuario.setTelefono(request.getTelefono());
        }
        if (request.getNotificacionesActivas() != null) {
            usuario.setNotificacionesActivas(request.getNotificacionesActivas());
        }
        if (request.getFinca() != null) {
            PerfilUpdateRequest.FincaUpdate finca = request.getFinca();
            if (finca.getNombre() != null) usuario.setNombreFinca(finca.getNombre());
            if (finca.getDepartamento() != null) usuario.setDepartamento(finca.getDepartamento());
            if (finca.getMunicipio() != null) usuario.setMunicipio(finca.getMunicipio());
            if (finca.getHectareas() != null) usuario.setHectareas(finca.getHectareas());
        }

        usuarioRepository.save(usuario);
        return getPerfil(email);
    }

    /**
     * Actualiza la foto de perfil del usuario.
     */
    @Transactional
    public String updateFoto(String email, MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("archivo_vacio");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("archivo_muy_grande");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("tipo_archivo_no_permitido");
        }

        byte[] imageBytes;
        try {
            imageBytes = file.getBytes();
        } catch (IOException e) {
            throw new IllegalArgumentException("archivo_invalido");
        }
        String detectedType = detectImageType(imageBytes);
        if (!contentType.equals(detectedType)) {
            throw new IllegalArgumentException("tipo_archivo_no_permitido");
        }

        Usuario usuario = usuarioRepository.findByIdentificador(email)
                .orElseThrow(() -> new IllegalArgumentException("usuario_no_encontrado"));

        try {
            // Crear directorio de uploads si no existe
            Path uploadPath = Paths.get(uploadsDir, "perfil");
            Files.createDirectories(uploadPath);

            // Eliminar fotos previas del usuario si existen
            String userPrefix = usuario.getId().toString();
            try (var stream = Files.list(uploadPath)) {
                stream.filter(p -> p.getFileName().toString().startsWith(userPrefix))
                      .forEach(p -> {
                          try {
                              Files.deleteIfExists(p);
                          } catch (IOException ignored) {}
                      });
            }

            // Generar nombre seguro para el archivo basado en MIME type
            String extension = getSafeExtension(file.getContentType());
            String fileName = userPrefix + extension;
            Path filePath = uploadPath.resolve(fileName);

            // Guardar archivo
            Files.write(filePath, imageBytes);

            // Actualizar URL en la base de datos
            String fotoUrl = baseUrl + "/uploads/perfil/" + fileName;
            usuario.setFotoPerfilUrl(fotoUrl);
            usuarioRepository.save(usuario);

            return fotoUrl;
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar la foto de perfil", e);
        }
    }

    private String getSafeExtension(String contentType) {
        if ("image/png".equalsIgnoreCase(contentType)) return ".png";
        if ("image/webp".equalsIgnoreCase(contentType)) return ".webp";
        return ".jpg";
    }

    private String detectImageType(byte[] bytes) {
        if (bytes.length >= 3 && (bytes[0] & 0xFF) == 0xFF && (bytes[1] & 0xFF) == 0xD8 && (bytes[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }
        if (bytes.length >= 8
                && (bytes[0] & 0xFF) == 0x89
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
}
