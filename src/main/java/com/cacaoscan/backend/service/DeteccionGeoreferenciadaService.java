package com.cacaoscan.backend.service;

import com.cacaoscan.backend.dto.DeteccionGeoreferenciadaDTOs.*;
import com.cacaoscan.backend.exception.ResourceNotFoundException;
import com.cacaoscan.backend.model.*;
import com.cacaoscan.backend.repository.*;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DeteccionGeoreferenciadaService {

    private static final Logger logger = LoggerFactory.getLogger(DeteccionGeoreferenciadaService.class);
    private static final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    private final DeteccionGeoreferenciadaRepository deteccionRepository;
    private final LoteRepository loteRepository;
    private final InspeccionRepository inspeccionRepository;
    private final UsuarioRepository usuarioRepository;

    public DeteccionGeoreferenciadaService(DeteccionGeoreferenciadaRepository deteccionRepository,
                                           LoteRepository loteRepository,
                                           InspeccionRepository inspeccionRepository,
                                           UsuarioRepository usuarioRepository) {
        this.deteccionRepository = deteccionRepository;
        this.loteRepository = loteRepository;
        this.inspeccionRepository = inspeccionRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Registra una detección de enfermedad georreferenciada.
     *
     * 1. Crea el Point a partir de lat/lng.
     * 2. Asigna automáticamente el lote mediante ST_Contains (PostGIS).
     * 3. Vincula a la inspección si se proporcionó inspeccionId.
     * 4. Actualiza el conteo de enfermedades de la inspección.
     */
    @Transactional
    public DeteccionResponse crearDeteccion(CrearDeteccionRequest request, String userEmail) {
        Usuario usuario = usuarioRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // Crear el punto geográfico
        Point punto = geometryFactory.createPoint(
                new Coordinate(request.getLongitud(), request.getLatitud())
        );
        punto.setSRID(4326);

        DeteccionGeoreferenciada deteccion = new DeteccionGeoreferenciada();
        deteccion.setUsuario(usuario);
        deteccion.setEnfermedad(request.getEnfermedad());
        deteccion.setPorcentajeConfianza(request.getPorcentajeConfianza());
        deteccion.setSeveridad(request.getSeveridad());
        deteccion.setImagenUrl(request.getImagenUrl());
        deteccion.setUbicacion(punto);
        deteccion.setObservaciones(request.getObservaciones());

        // Asignar automáticamente el lote que contiene este punto
        loteRepository.findByContainingPoint(punto).ifPresent(lote -> {
            deteccion.setLote(lote);
            logger.info("Detección asignada automáticamente al lote '{}'", lote.getNombre());
        });

        // Vincular a inspección si existe
        if (request.getInspeccionId() != null) {
            Inspeccion inspeccion = inspeccionRepository.findById(request.getInspeccionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Inspección no encontrada"));
            deteccion.setInspeccion(inspeccion);

            // Actualizar conteo de la inspección
            inspeccion.setTotalEnfermedades(inspeccion.getTotalEnfermedades() + 1);
            inspeccionRepository.save(inspeccion);
        }

        DeteccionGeoreferenciada saved = deteccionRepository.save(deteccion);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<DeteccionResponse> listarPorLote(UUID loteId, String userEmail) {
        Lote lote = loteRepository.findById(loteId)
                .orElseThrow(() -> new ResourceNotFoundException("Lote no encontrado"));

        validarAcceso(lote, userEmail);

        return deteccionRepository.findByLoteOrderByFechaDeteccionDesc(lote)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DeteccionResponse obtenerDetalle(UUID deteccionId, String userEmail) {
        DeteccionGeoreferenciada deteccion = deteccionRepository.findById(deteccionId)
                .orElseThrow(() -> new ResourceNotFoundException("Detección no encontrada"));

        return toResponse(deteccion);
    }

    /**
     * Obtiene todas las detecciones del usuario para la vista global del mapa.
     */
    @Transactional(readOnly = true)
    public List<DeteccionMarkerResponse> obtenerTodasMapa(String userEmail) {
        Usuario usuario = usuarioRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        return deteccionRepository.findAllByUsuarioId(usuario.getId())
                .stream()
                .map(this::toMarkerResponse)
                .collect(Collectors.toList());
    }

    // ======================== Mappers ========================

    private DeteccionResponse toResponse(DeteccionGeoreferenciada d) {
        DeteccionResponse response = new DeteccionResponse();
        response.setId(d.getId());
        response.setLoteId(d.getLote() != null ? d.getLote().getId() : null);
        response.setLoteNombre(d.getLote() != null ? d.getLote().getNombre() : null);
        response.setInspeccionId(d.getInspeccion() != null ? d.getInspeccion().getId() : null);
        response.setEnfermedad(d.getEnfermedad());
        response.setPorcentajeConfianza(d.getPorcentajeConfianza());
        response.setSeveridad(d.getSeveridad());
        response.setImagenUrl(d.getImagenUrl());
        if (d.getUbicacion() != null) {
            response.setLatitud(d.getUbicacion().getY());
            response.setLongitud(d.getUbicacion().getX());
        }
        response.setObservaciones(d.getObservaciones());
        response.setFechaDeteccion(d.getFechaDeteccion());
        return response;
    }

    private DeteccionMarkerResponse toMarkerResponse(DeteccionGeoreferenciada d) {
        DeteccionMarkerResponse marker = new DeteccionMarkerResponse();
        marker.setId(d.getId());
        marker.setEnfermedad(d.getEnfermedad());
        marker.setPorcentajeConfianza(d.getPorcentajeConfianza());
        marker.setSeveridad(d.getSeveridad());
        if (d.getUbicacion() != null) {
            marker.setLatitud(d.getUbicacion().getY());
            marker.setLongitud(d.getUbicacion().getX());
        }
        marker.setFechaDeteccion(d.getFechaDeteccion());
        return marker;
    }

    private void validarAcceso(Lote lote, String userEmail) {
        if (!lote.getFinca().getUsuario().getEmail().equals(userEmail)) {
            throw new IllegalArgumentException("No tienes permisos sobre este lote");
        }
    }
}
