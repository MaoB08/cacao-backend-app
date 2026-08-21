package com.cacaoscan.backend.service;

import com.cacaoscan.backend.dto.DeteccionGeoreferenciadaDTOs;
import com.cacaoscan.backend.dto.InspeccionDTOs.*;
import com.cacaoscan.backend.exception.ResourceNotFoundException;
import com.cacaoscan.backend.model.*;
import com.cacaoscan.backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class InspeccionService {

    private final InspeccionRepository inspeccionRepository;
    private final LoteRepository loteRepository;
    private final UsuarioRepository usuarioRepository;
    private final DeteccionGeoreferenciadaRepository deteccionRepository;

    public InspeccionService(InspeccionRepository inspeccionRepository,
                            LoteRepository loteRepository,
                            UsuarioRepository usuarioRepository,
                            DeteccionGeoreferenciadaRepository deteccionRepository) {
        this.inspeccionRepository = inspeccionRepository;
        this.loteRepository = loteRepository;
        this.usuarioRepository = usuarioRepository;
        this.deteccionRepository = deteccionRepository;
    }

    @Transactional
    public InspeccionResponse crearInspeccion(CrearInspeccionRequest request, String userEmail) {
        Usuario usuario = usuarioRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Lote lote = loteRepository.findById(request.getLoteId())
                .orElseThrow(() -> new ResourceNotFoundException("Lote no encontrado"));

        validarAcceso(lote, userEmail);

        Inspeccion inspeccion = new Inspeccion();
        inspeccion.setLote(lote);
        inspeccion.setUsuario(usuario);
        inspeccion.setPlantasInspeccionadas(request.getPlantasInspeccionadas());
        inspeccion.setPlantasEnfermas(request.getPlantasEnfermas());
        inspeccion.setObservaciones(request.getObservaciones());
        inspeccion.setDuracionMinutos(request.getDuracionMinutos());
        inspeccion.setEstado(EstadoInspeccion.COMPLETADA);

        Inspeccion saved = inspeccionRepository.save(inspeccion);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<InspeccionResponse> listarPorLote(UUID loteId, String userEmail) {
        Lote lote = loteRepository.findById(loteId)
                .orElseThrow(() -> new ResourceNotFoundException("Lote no encontrado"));

        validarAcceso(lote, userEmail);

        return inspeccionRepository.findByLoteOrderByFechaInspeccionDesc(lote)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public InspeccionDetalleResponse obtenerDetalle(UUID inspeccionId, String userEmail) {
        Inspeccion inspeccion = inspeccionRepository.findById(inspeccionId)
                .orElseThrow(() -> new ResourceNotFoundException("Inspección no encontrada"));

        validarAcceso(inspeccion.getLote(), userEmail);

        InspeccionDetalleResponse response = new InspeccionDetalleResponse();
        copiarCamposBase(inspeccion, response);

        // Cargar detecciones asociadas a esta inspección
        List<DeteccionGeoreferenciadaDTOs.DeteccionResponse> detecciones =
                deteccionRepository.findByInspeccionId(inspeccionId)
                        .stream()
                        .map(this::toDeteccionResponse)
                        .collect(Collectors.toList());
        response.setDetecciones(detecciones);

        // Actualizar el conteo total de enfermedades
        response.setTotalEnfermedades(detecciones.size());

        return response;
    }

    // ======================== Mappers ========================

    private InspeccionResponse toResponse(Inspeccion inspeccion) {
        InspeccionResponse response = new InspeccionResponse();
        copiarCamposBase(inspeccion, response);
        return response;
    }

    private void copiarCamposBase(Inspeccion inspeccion, InspeccionResponse response) {
        response.setId(inspeccion.getId());
        response.setLoteId(inspeccion.getLote().getId());
        response.setLoteNombre(inspeccion.getLote().getNombre());
        response.setUsuarioNombre(inspeccion.getUsuario().getNombre());
        response.setFechaInspeccion(inspeccion.getFechaInspeccion());
        response.setPlantasInspeccionadas(inspeccion.getPlantasInspeccionadas());
        response.setPlantasEnfermas(inspeccion.getPlantasEnfermas());
        response.setTotalEnfermedades(inspeccion.getTotalEnfermedades());
        response.setObservaciones(inspeccion.getObservaciones());
        response.setEstado(inspeccion.getEstado().name());
        response.setDuracionMinutos(inspeccion.getDuracionMinutos());
        response.setCreatedAt(inspeccion.getCreatedAt());
    }

    private DeteccionGeoreferenciadaDTOs.DeteccionResponse toDeteccionResponse(DeteccionGeoreferenciada d) {
        DeteccionGeoreferenciadaDTOs.DeteccionResponse response = new DeteccionGeoreferenciadaDTOs.DeteccionResponse();
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

    private void validarAcceso(Lote lote, String userEmail) {
        if (!lote.getFinca().getUsuario().getEmail().equals(userEmail)) {
            throw new IllegalArgumentException("No tienes permisos sobre este lote");
        }
    }
}
