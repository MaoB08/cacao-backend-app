package com.cacaoscan.backend.service;

import com.cacaoscan.backend.dto.AgricultorVisitaDTOs.*;
import com.cacaoscan.backend.model.*;
import com.cacaoscan.backend.repository.UsuarioRepository;
import com.cacaoscan.backend.repository.VisitaTecnicaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Servicio para la vista de visitas técnicas del agricultor.
 * El agricultor solo puede ver visitas asociadas a sus propias fincas.
 * Se excluyen notas internas del agrónomo (notasPrevias) y observaciones internas;
 * solo se exponen las recomendaciones compartidas y datos de ejecución.
 */
@Service
public class AgricultorVisitaService {

    private static final int MAX_PROXIMAS_DASHBOARD = 3;

    private final VisitaTecnicaRepository visitaRepository;
    private final UsuarioRepository usuarioRepository;

    public AgricultorVisitaService(VisitaTecnicaRepository visitaRepository,
                                    UsuarioRepository usuarioRepository) {
        this.visitaRepository = visitaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Lista visitas asociadas a fincas del agricultor autenticado.
     * Excluye notas internas del agrónomo.
     */
    @Transactional(readOnly = true)
    public Page<VisitaAgricultorResumen> listarVisitasAgricultor(String userEmail, UUID fincaId,
                                                                  Pageable pageable) {
        Usuario agricultor = obtenerAgricultor(userEmail);
        Page<VisitaTecnica> visitas = visitaRepository.findByAgricultorWithFilters(
                agricultor, fincaId, pageable);
        return visitas.map(this::toResumenAgricultor);
    }

    /**
     * Obtiene información resumida de visitas para el dashboard del agricultor.
     */
    @Transactional(readOnly = true)
    public VisitasAgricultorDashboardInfo obtenerDashboardVisitas(String userEmail) {
        Usuario agricultor = obtenerAgricultor(userEmail);

        long proximas = visitaRepository.countProximasVisitasAgricultor(agricultor);
        long completadas = visitaRepository.countByAgronomoAndEstado(agricultor, EstadoVisita.COMPLETADA);
        // Nota: countByAgronomoAndEstado busca por el campo agronomo.
        // Necesitamos contar completadas del agricultor.
        // Usaremos una consulta alternativa — contar a partir de la lista.
        Page<VisitaTecnica> todasVisitas = visitaRepository.findByAgricultorWithFilters(
                agricultor, null, PageRequest.of(0, 1000));
        long completadasTotal = todasVisitas.getContent().stream()
                .filter(v -> v.getEstado() == EstadoVisita.COMPLETADA)
                .count();

        List<VisitaTecnica> proximasVisitas = visitaRepository.findProximasVisitasAgricultor(
                agricultor, LocalDateTime.now(),
                PageRequest.of(0, MAX_PROXIMAS_DASHBOARD));

        List<ProximaVisitaAgricultorInfo> proximasInfo = proximasVisitas.stream()
                .map(v -> {
                    ProximaVisitaAgricultorInfo info = new ProximaVisitaAgricultorInfo();
                    info.setId(v.getId());
                    info.setEstado(v.getEstado().name());
                    info.setFechaProgramada(v.getFechaProgramada());
                    info.setFincaNombre(v.getFinca().getNombre());
                    info.setAgronomoNombre(v.getAgronomo().getNombre());
                    info.setMotivo(v.getMotivo());
                    info.setLoteNombre(v.getLote() != null ? v.getLote().getNombre() : null);
                    return info;
                })
                .collect(Collectors.toList());

        return new VisitasAgricultorDashboardInfo(proximas, completadasTotal, proximasInfo);
    }

    // ======================== Helpers ========================

    private Usuario obtenerAgricultor(String userEmail) {
        Usuario usuario = usuarioRepository.findByIdentificador(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        if (usuario.getRol() != Rol.AGRICULTOR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Solo los agricultores pueden consultar este recurso");
        }
        return usuario;
    }

    /**
     * Mapea VisitaTecnica a VisitaAgricultorResumen, excluyendo notas internas.
     * - NO incluye notasPrevias (notas privadas del agrónomo antes de la visita)
     * - NO incluye observaciones (notas internas del agrónomo durante la visita)
     * - SÍ incluye recomendaciones (dirigidas al agricultor)
     * - SÍ incluye datos de ejecución (fechas, duración, conteo de plantas)
     */
    private VisitaAgricultorResumen toResumenAgricultor(VisitaTecnica v) {
        VisitaAgricultorResumen r = new VisitaAgricultorResumen();
        r.setId(v.getId());
        r.setEstado(v.getEstado().name());
        r.setFechaProgramada(v.getFechaProgramada());
        r.setDuracionEstimadaMin(v.getDuracionEstimadaMin());
        r.setMotivo(v.getMotivo());

        // Finca
        r.setFincaId(v.getFinca().getId());
        r.setFincaNombre(v.getFinca().getNombre());
        r.setFincaMunicipio(v.getFinca().getMunicipio());

        // Agrónomo (solo nombre profesional)
        r.setAgronomoNombre(v.getAgronomo().getNombre());

        // Lote
        if (v.getLote() != null) {
            r.setLoteId(v.getLote().getId());
            r.setLoteNombre(v.getLote().getNombre());
        }

        // Ejecución (si la visita fue realizada)
        r.setFechaInicioReal(v.getFechaInicioReal());
        r.setFechaFinReal(v.getFechaFinReal());
        r.setDuracionRealMin(v.getDuracionRealMin());

        // Recomendaciones compartidas (SIN observaciones internas)
        r.setRecomendaciones(v.getRecomendaciones());
        r.setPlantasRevisadas(v.getPlantasRevisadas());
        r.setPlantasEnfermas(v.getPlantasEnfermas());

        r.setCreatedAt(v.getCreatedAt());
        return r;
    }
}
