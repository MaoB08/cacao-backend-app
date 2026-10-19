package com.cacaoscan.backend.service;

import com.cacaoscan.backend.dto.AgronomoDashboardResponse;
import com.cacaoscan.backend.dto.VisitaDTOs.*;
import com.cacaoscan.backend.exception.ResourceNotFoundException;
import com.cacaoscan.backend.model.*;
import com.cacaoscan.backend.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class VisitaTecnicaService {

    private static final int DEFAULT_DURACION_MIN = 60;
    private static final int MAX_PROXIMAS_VISITAS_DASHBOARD = 5;

    private final VisitaTecnicaRepository visitaRepository;
    private final FincaRepository fincaRepository;
    private final LoteRepository loteRepository;
    private final UsuarioRepository usuarioRepository;
    private final AgronomoFincaService agronomoFincaService;
    private final VisitaImagenRepository visitaImagenRepository;

    public VisitaTecnicaService(VisitaTecnicaRepository visitaRepository,
                                FincaRepository fincaRepository,
                                LoteRepository loteRepository,
                                UsuarioRepository usuarioRepository,
                                AgronomoFincaService agronomoFincaService,
                                VisitaImagenRepository visitaImagenRepository) {
        this.visitaRepository = visitaRepository;
        this.fincaRepository = fincaRepository;
        this.loteRepository = loteRepository;
        this.usuarioRepository = usuarioRepository;
        this.agronomoFincaService = agronomoFincaService;
        this.visitaImagenRepository = visitaImagenRepository;
    }

    // ======================== Crear visita ========================

    @Transactional
    public VisitaDetalleResponse crearVisita(CrearVisitaRequest request, String userEmail) {
        Usuario agronomo = agronomoFincaService.obtenerAgronomo(userEmail);

        // Validar finca y asignación
        Finca finca = fincaRepository.findById(request.getFincaId())
                .orElseThrow(() -> new ResourceNotFoundException("Finca no encontrada"));
        agronomoFincaService.validarAsignacion(agronomo, finca);

        // Validar lote (opcional)
        Lote lote = null;
        if (request.getLoteId() != null) {
            lote = loteRepository.findById(request.getLoteId())
                    .orElseThrow(() -> new ResourceNotFoundException("Lote no encontrado"));
            if (!lote.getFinca().getId().equals(finca.getId())) {
                throw new IllegalArgumentException("El lote no pertenece a la finca seleccionada");
            }
        }

        // Validar fecha futura
        if (request.getFechaProgramada().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("La fecha programada debe ser futura");
        }

        // Detectar conflictos de horario
        int duracion = request.getDuracionEstimadaMin() != null
                ? request.getDuracionEstimadaMin() : DEFAULT_DURACION_MIN;
        LocalDateTime finEstimado = request.getFechaProgramada().plusMinutes(duracion);

        long conflictos = visitaRepository.countConflictos(
                agronomo.getId(),
                request.getFechaProgramada(),
                finEstimado,
                UUID.randomUUID()); // No excluir ninguna visita existente
        if (conflictos > 0) {
            throw new IllegalArgumentException(
                    "Ya tienes una visita programada en ese horario. Revisa tu agenda.");
        }

        // Crear entidad
        VisitaTecnica visita = new VisitaTecnica();
        visita.setAgronomo(agronomo);
        visita.setFinca(finca);
        visita.setLote(lote);
        visita.setAgricultor(finca.getUsuario());
        visita.setFechaProgramada(request.getFechaProgramada());
        visita.setDuracionEstimadaMin(request.getDuracionEstimadaMin());
        visita.setMotivo(request.getMotivo());
        visita.setNotasPrevias(request.getNotasPrevias());
        visita.setEstado(EstadoVisita.PROGRAMADA);

        VisitaTecnica saved = visitaRepository.save(visita);
        return toDetalleResponse(saved);
    }

    // ======================== Listar visitas ========================

    @Transactional(readOnly = true)
    public Page<VisitaResumenResponse> listarVisitas(String userEmail, String estado,
                                                      UUID fincaId, LocalDateTime desde,
                                                      LocalDateTime hasta, Pageable pageable) {
        Usuario agronomo = agronomoFincaService.obtenerAgronomo(userEmail);

        EstadoVisita estadoEnum = null;
        if (estado != null && !estado.isBlank()) {
            try {
                estadoEnum = EstadoVisita.valueOf(estado.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Estado no válido: " + estado);
            }
        }

        Page<VisitaTecnica> visitas = visitaRepository.findByAgronomoWithFilters(
                agronomo, estadoEnum, fincaId, desde, hasta, pageable);

        return visitas.map(this::toResumenResponse);
    }

    // ======================== Detalle ========================

    @Transactional(readOnly = true)
    public VisitaDetalleResponse obtenerDetalle(UUID visitaId, String userEmail) {
        Usuario agronomo = agronomoFincaService.obtenerAgronomo(userEmail);
        VisitaTecnica visita = obtenerVisitaDelAgronomo(visitaId, agronomo);
        return toDetalleResponse(visita);
    }

    // ======================== Reprogramar ========================

    @Transactional
    public VisitaDetalleResponse reprogramarVisita(UUID visitaId, ReprogramarVisitaRequest request,
                                                    String userEmail) {
        Usuario agronomo = agronomoFincaService.obtenerAgronomo(userEmail);
        VisitaTecnica visita = obtenerVisitaDelAgronomo(visitaId, agronomo);

        // Solo se puede reprogramar si está PROGRAMADA
        if (visita.getEstado() != EstadoVisita.PROGRAMADA) {
            throw new IllegalArgumentException(
                    "Solo se pueden reprogramar visitas en estado PROGRAMADA. Estado actual: " + visita.getEstado());
        }

        // Validar fecha futura
        if (request.getFechaProgramada().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("La nueva fecha debe ser futura");
        }

        // Detectar conflictos
        int duracion = request.getDuracionEstimadaMin() != null
                ? request.getDuracionEstimadaMin()
                : (visita.getDuracionEstimadaMin() != null ? visita.getDuracionEstimadaMin() : DEFAULT_DURACION_MIN);
        LocalDateTime finEstimado = request.getFechaProgramada().plusMinutes(duracion);

        long conflictos = visitaRepository.countConflictos(
                agronomo.getId(), request.getFechaProgramada(), finEstimado, visita.getId());
        if (conflictos > 0) {
            throw new IllegalArgumentException("Hay un conflicto de horario con la nueva fecha");
        }

        visita.setFechaProgramada(request.getFechaProgramada());
        if (request.getDuracionEstimadaMin() != null) {
            visita.setDuracionEstimadaMin(request.getDuracionEstimadaMin());
        }
        if (request.getMotivo() != null) {
            visita.setMotivo(request.getMotivo());
        }
        if (request.getNotasPrevias() != null) {
            visita.setNotasPrevias(request.getNotasPrevias());
        }

        VisitaTecnica saved = visitaRepository.save(visita);
        return toDetalleResponse(saved);
    }

    // ======================== Cancelar ========================

    @Transactional
    public VisitaDetalleResponse cancelarVisita(UUID visitaId, CancelarVisitaRequest request,
                                                 String userEmail) {
        Usuario agronomo = agronomoFincaService.obtenerAgronomo(userEmail);
        VisitaTecnica visita = obtenerVisitaDelAgronomo(visitaId, agronomo);

        if (visita.getEstado() != EstadoVisita.PROGRAMADA && visita.getEstado() != EstadoVisita.EN_CURSO) {
            throw new IllegalArgumentException(
                    "Solo se pueden cancelar visitas PROGRAMADAS o EN_CURSO. Estado actual: " + visita.getEstado());
        }

        visita.setEstado(EstadoVisita.CANCELADA);
        visita.setMotivoCancelacion(request.getMotivoCancelacion());

        VisitaTecnica saved = visitaRepository.save(visita);
        return toDetalleResponse(saved);
    }

    // ======================== Iniciar ========================

    @Transactional
    public VisitaDetalleResponse iniciarVisita(UUID visitaId, String userEmail) {
        Usuario agronomo = agronomoFincaService.obtenerAgronomo(userEmail);
        VisitaTecnica visita = obtenerVisitaDelAgronomo(visitaId, agronomo);

        if (visita.getEstado() != EstadoVisita.PROGRAMADA) {
            throw new IllegalArgumentException(
                    "Solo se pueden iniciar visitas PROGRAMADAS. Estado actual: " + visita.getEstado());
        }

        visita.setEstado(EstadoVisita.EN_CURSO);
        visita.setFechaInicioReal(LocalDateTime.now());

        VisitaTecnica saved = visitaRepository.save(visita);
        return toDetalleResponse(saved);
    }

    // ======================== Finalizar ========================

    @Transactional
    public VisitaDetalleResponse finalizarVisita(UUID visitaId, FinalizarVisitaRequest request,
                                                  String userEmail) {
        Usuario agronomo = agronomoFincaService.obtenerAgronomo(userEmail);
        VisitaTecnica visita = obtenerVisitaDelAgronomo(visitaId, agronomo);

        if (visita.getEstado() != EstadoVisita.EN_CURSO) {
            throw new IllegalArgumentException(
                    "Solo se pueden finalizar visitas EN_CURSO. Estado actual: " + visita.getEstado());
        }

        LocalDateTime ahora = LocalDateTime.now();
        visita.setEstado(EstadoVisita.COMPLETADA);
        visita.setFechaFinReal(ahora);

        // Calcular duración real
        if (visita.getFechaInicioReal() != null) {
            long minutos = java.time.Duration.between(visita.getFechaInicioReal(), ahora).toMinutes();
            visita.setDuracionRealMin((int) minutos);
        }

        if (request != null) {
            if (request.getObservaciones() != null) visita.setObservaciones(request.getObservaciones());
            if (request.getRecomendaciones() != null) visita.setRecomendaciones(request.getRecomendaciones());
            if (request.getPlantasRevisadas() != null) visita.setPlantasRevisadas(request.getPlantasRevisadas());
            if (request.getPlantasEnfermas() != null) visita.setPlantasEnfermas(request.getPlantasEnfermas());
            if (request.getInformeFinal() != null) visita.setInformeFinal(request.getInformeFinal());
        }

        VisitaTecnica saved = visitaRepository.save(visita);
        return toDetalleResponse(saved);
    }

    // ======================== Dashboard (MA-07) ========================

    @Transactional(readOnly = true)
    public AgronomoDashboardResponse obtenerDashboard(String userEmail) {
        Usuario agronomo = agronomoFincaService.obtenerAgronomo(userEmail);

        // Info del agrónomo
        long fincasAsignadas = agronomoFincaService.contarFincasAsignadas(agronomo);
        AgronomoDashboardResponse.AgronomoInfo agronomoInfo =
                new AgronomoDashboardResponse.AgronomoInfo(agronomo.getNombre(), fincasAsignadas);

        // Contadores
        long programadas = visitaRepository.countByAgronomoAndEstado(agronomo, EstadoVisita.PROGRAMADA);
        long enCurso = visitaRepository.countByAgronomoAndEstado(agronomo, EstadoVisita.EN_CURSO);
        LocalDateTime inicioMes = LocalDateTime.now()
                .with(TemporalAdjusters.firstDayOfMonth())
                .withHour(0).withMinute(0).withSecond(0).withNano(0);
        long completadasMes = visitaRepository.countCompletadasDesde(agronomo, inicioMes);
        AgronomoDashboardResponse.ContadoresInfo contadores =
                new AgronomoDashboardResponse.ContadoresInfo(programadas, enCurso, completadasMes);

        // Próximas visitas
        List<VisitaTecnica> proximas = visitaRepository.findProximasVisitas(
                agronomo,
                LocalDateTime.now(),
                PageRequest.of(0, MAX_PROXIMAS_VISITAS_DASHBOARD));

        List<AgronomoDashboardResponse.ProximaVisitaInfo> proximasInfo = proximas.stream()
                .map(v -> {
                    AgronomoDashboardResponse.ProximaVisitaInfo info = new AgronomoDashboardResponse.ProximaVisitaInfo();
                    info.setId(v.getId());
                    info.setEstado(v.getEstado().name());
                    info.setFechaProgramada(v.getFechaProgramada());
                    info.setFincaNombre(v.getFinca().getNombre());
                    info.setFincaMunicipio(v.getFinca().getMunicipio());
                    info.setAgricultorNombre(v.getAgricultor().getNombre());
                    info.setLoteNombre(v.getLote() != null ? v.getLote().getNombre() : null);
                    info.setMotivo(v.getMotivo());
                    return info;
                })
                .collect(Collectors.toList());

        return new AgronomoDashboardResponse(agronomoInfo, contadores, proximasInfo);
    }

    // ======================== Helpers privados ========================

    private VisitaTecnica obtenerVisitaDelAgronomo(UUID visitaId, Usuario agronomo) {
        VisitaTecnica visita = visitaRepository.findById(visitaId)
                .orElseThrow(() -> new ResourceNotFoundException("Visita no encontrada"));

        if (!visita.getAgronomo().getId().equals(agronomo.getId())) {
            throw new IllegalArgumentException("No tienes permisos sobre esta visita");
        }
        return visita;
    }

    // ======================== Mappers ========================

    private VisitaResumenResponse toResumenResponse(VisitaTecnica v) {
        VisitaResumenResponse r = new VisitaResumenResponse();
        r.setId(v.getId());
        r.setEstado(v.getEstado().name());
        r.setFechaProgramada(v.getFechaProgramada());
        r.setDuracionEstimadaMin(v.getDuracionEstimadaMin());
        r.setMotivo(v.getMotivo());
        r.setFincaId(v.getFinca().getId());
        r.setFincaNombre(v.getFinca().getNombre());
        r.setFincaMunicipio(v.getFinca().getMunicipio());
        r.setAgricultorNombre(v.getAgricultor().getNombre());
        if (v.getLote() != null) {
            r.setLoteId(v.getLote().getId());
            r.setLoteNombre(v.getLote().getNombre());
            r.setLoteVariedadCacao(v.getLote().getVariedadCacao());
        }
        r.setCreatedAt(v.getCreatedAt());
        return r;
    }

    private VisitaDetalleResponse toDetalleResponse(VisitaTecnica v) {
        VisitaDetalleResponse r = new VisitaDetalleResponse();
        // Base
        r.setId(v.getId());
        r.setEstado(v.getEstado().name());
        r.setFechaProgramada(v.getFechaProgramada());
        r.setDuracionEstimadaMin(v.getDuracionEstimadaMin());
        r.setMotivo(v.getMotivo());
        r.setNotasPrevias(v.getNotasPrevias());

        // Finca
        r.setFincaId(v.getFinca().getId());
        r.setFincaNombre(v.getFinca().getNombre());
        r.setFincaMunicipio(v.getFinca().getMunicipio());
        r.setFincaDepartamento(v.getFinca().getDepartamento());
        r.setFincaVereda(v.getFinca().getVereda());

        // Agricultor
        r.setAgricultorId(v.getAgricultor().getId());
        r.setAgricultorNombre(v.getAgricultor().getNombre());
        r.setAgricultorTelefono(v.getAgricultor().getTelefono());

        // Lote
        if (v.getLote() != null) {
            r.setLoteId(v.getLote().getId());
            r.setLoteNombre(v.getLote().getNombre());
            r.setLoteVariedadCacao(v.getLote().getVariedadCacao());
        }

        // Ejecución
        r.setFechaInicioReal(v.getFechaInicioReal());
        r.setFechaFinReal(v.getFechaFinReal());
        r.setDuracionRealMin(v.getDuracionRealMin());

        // Resultados
        r.setObservaciones(v.getObservaciones());
        r.setRecomendaciones(v.getRecomendaciones());
        r.setPlantasRevisadas(v.getPlantasRevisadas());
        r.setPlantasEnfermas(v.getPlantasEnfermas());
        r.setInformeFinal(v.getInformeFinal());

        // Cancelación
        r.setMotivoCancelacion(v.getMotivoCancelacion());

        // Imágenes
        long totalImagenes = visitaImagenRepository.countByVisita(v);
        r.setTotalImagenes((int) totalImagenes);

        List<VisitaImagenResponse> imagenes = visitaImagenRepository.findByVisitaOrderByCreatedAtDesc(v)
                .stream()
                .map(img -> {
                    VisitaImagenResponse imgR = new VisitaImagenResponse();
                    imgR.setId(img.getId());
                    imgR.setImagenUrl(img.getImagenUrl());
                    imgR.setNotas(img.getNotas());
                    if (img.getLote() != null) {
                        imgR.setLoteId(img.getLote().getId());
                        imgR.setLoteNombre(img.getLote().getNombre());
                    }
                    if (img.getAnalisis() != null) {
                        imgR.setAnalisisId(img.getAnalisis().getId());
                        imgR.setEnfermedadDetectada(img.getAnalisis().getNombreEnfermedad());
                        imgR.setConfianza(img.getAnalisis().getConfianza());
                        imgR.setTieneEnfermedad(img.getAnalisis().isTieneEnfermedad());
                    }
                    imgR.setCreatedAt(img.getCreatedAt());
                    return imgR;
                })
                .collect(Collectors.toList());
        r.setImagenes(imagenes);

        r.setCreatedAt(v.getCreatedAt());
        r.setUpdatedAt(v.getUpdatedAt());
        return r;
    }
}
