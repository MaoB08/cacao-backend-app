package com.cacaoscan.backend.service;

import com.cacaoscan.backend.dto.DeteccionGeoreferenciadaDTOs;
import com.cacaoscan.backend.dto.LoteDTOs.*;
import com.cacaoscan.backend.exception.InvalidPolygonException;
import com.cacaoscan.backend.exception.ResourceNotFoundException;
import com.cacaoscan.backend.model.*;
import com.cacaoscan.backend.repository.*;
import org.locationtech.jts.geom.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LoteService {

    private static final Logger logger = LoggerFactory.getLogger(LoteService.class);
    private static final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    /**
     * Paleta de colores predefinida para asignar automáticamente a lotes nuevos
     * cuando el usuario no especifica un color. Colores diferenciados y profesionales.
     */
    private static final String[] COLORES_LOTE = {
            "#4CAF50", "#FF9800", "#2196F3", "#9C27B0",
            "#009688", "#FF5722", "#3F51B5", "#8BC34A",
            "#E91E63", "#00BCD4", "#FFC107", "#795548"
    };

    private final LoteRepository loteRepository;
    private final FincaRepository fincaRepository;
    private final UsuarioRepository usuarioRepository;
    private final InspeccionRepository inspeccionRepository;
    private final DeteccionGeoreferenciadaRepository deteccionRepository;

    public LoteService(LoteRepository loteRepository,
                       FincaRepository fincaRepository,
                       UsuarioRepository usuarioRepository,
                       InspeccionRepository inspeccionRepository,
                       DeteccionGeoreferenciadaRepository deteccionRepository) {
        this.loteRepository = loteRepository;
        this.fincaRepository = fincaRepository;
        this.usuarioRepository = usuarioRepository;
        this.inspeccionRepository = inspeccionRepository;
        this.deteccionRepository = deteccionRepository;
    }

    /**
     * Crea un nuevo lote a partir de las coordenadas GPS capturadas
     * durante el recorrido del agricultor.
     *
     * 1. Convierte la lista de CoordenadaDTO a un Polygon JTS (SRID 4326).
     * 2. Valida que el polígono sea topológicamente correcto.
     * 3. Persiste en PostGIS.
     * 4. Calcula área y perímetro mediante funciones geodésicas de PostGIS.
     */
    @Transactional
    public LoteDetalleResponse crearLote(CrearLoteRequest request, String userEmail) {
        Usuario usuario = usuarioRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Finca finca = fincaRepository.findById(request.getFincaId())
                .orElseThrow(() -> new ResourceNotFoundException("Finca no encontrada"));

        if (!finca.getUsuario().getId().equals(usuario.getId())) {
            throw new IllegalArgumentException("No tienes permisos sobre esta finca");
        }

        if (loteRepository.existsByFincaAndNombreIgnoreCase(finca, request.getNombre())) {
            throw new IllegalArgumentException("Ya existe un lote con ese nombre en esta finca");
        }

        // Construir el polígono a partir de las coordenadas del recorrido GPS
        Polygon polygon = construirPoligono(request.getCoordenadas());

        // Asignar color automático si no fue especificado
        String color = request.getColorHex();
        if (color == null || color.isBlank()) {
            long totalLotes = finca.getLotes().size();
            color = COLORES_LOTE[(int) (totalLotes % COLORES_LOTE.length)];
        }

        Lote lote = new Lote();
        lote.setFinca(finca);
        lote.setNombre(request.getNombre());
        lote.setVariedadCacao(request.getVariedadCacao());
        lote.setAnoSiembra(request.getAnoSiembra());
        lote.setNumeroPlantas(request.getNumeroPlantas());
        lote.setGeometria(polygon);
        lote.setColorHex(color);
        lote.setNotas(request.getNotas());

        Lote saved = loteRepository.save(lote);

        // Calcular área y perímetro geodésicos mediante PostGIS
        Double areaHa = loteRepository.calcularAreaHectareas(saved.getId());
        Double perimetroM = loteRepository.calcularPerimetroMetros(saved.getId());

        saved.setAreaHectareas(areaHa != null
                ? BigDecimal.valueOf(areaHa).setScale(4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO);
        saved.setPerimetroMetros(perimetroM != null
                ? BigDecimal.valueOf(perimetroM).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO);

        loteRepository.save(saved);

        // Recalcular el área total de la finca sumando todos los lotes
        recalcularAreaFinca(finca);

        logger.info("Lote '{}' creado exitosamente. Área: {} ha, Perímetro: {} m",
                saved.getNombre(), saved.getAreaHectareas(), saved.getPerimetroMetros());

        return toDetalleResponse(saved);
    }

    /**
     * Obtiene todos los lotes del usuario con la información necesaria
     * para renderizar los polígonos y marcadores en el mapa.
     */
    @Transactional(readOnly = true)
    public List<LoteMapaResponse> obtenerLotesMapa(String userEmail) {
        Usuario usuario = usuarioRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        List<Lote> lotes = loteRepository.findAllByUsuarioId(usuario.getId());

        return lotes.stream().map(lote -> {
            LoteMapaResponse response = new LoteMapaResponse();
            response.setId(lote.getId());
            response.setNombre(lote.getNombre());
            response.setAreaHectareas(lote.getAreaHectareas());
            response.setColorHex(lote.getColorHex());
            response.setEstado(lote.getEstado().name());
            response.setCoordenadas(extraerCoordenadas(lote.getGeometria()));

            // Cargar marcadores de detecciones para el mapa
            List<DeteccionGeoreferenciadaDTOs.DeteccionMarkerResponse> markers =
                    lote.getDetecciones().stream()
                            .map(this::toMarkerResponse)
                            .collect(Collectors.toList());
            response.setDetecciones(markers);

            return response;
        }).collect(Collectors.toList());
    }

    /**
     * Obtiene el detalle completo de un lote incluyendo estadísticas
     * de inspecciones y resumen de enfermedades.
     */
    @Transactional(readOnly = true)
    public LoteDetalleResponse obtenerDetalle(UUID loteId, String userEmail) {
        Lote lote = loteRepository.findById(loteId)
                .orElseThrow(() -> new ResourceNotFoundException("Lote no encontrado"));

        validarPropietario(lote, userEmail);
        return toDetalleResponse(lote);
    }

    @Transactional
    public LoteDetalleResponse actualizarLote(UUID loteId, ActualizarLoteRequest request, String userEmail) {
        Lote lote = loteRepository.findById(loteId)
                .orElseThrow(() -> new ResourceNotFoundException("Lote no encontrado"));

        validarPropietario(lote, userEmail);

        if (request.getNombre() != null && !request.getNombre().isBlank()) {
            lote.setNombre(request.getNombre());
        }
        if (request.getVariedadCacao() != null) {
            lote.setVariedadCacao(request.getVariedadCacao());
        }
        if (request.getAnoSiembra() != null) {
            lote.setAnoSiembra(request.getAnoSiembra());
        }
        if (request.getNumeroPlantas() != null) {
            lote.setNumeroPlantas(request.getNumeroPlantas());
        }
        if (request.getColorHex() != null && !request.getColorHex().isBlank()) {
            lote.setColorHex(request.getColorHex());
        }
        if (request.getEstado() != null) {
            lote.setEstado(EstadoLote.valueOf(request.getEstado()));
        }
        if (request.getNotas() != null) {
            lote.setNotas(request.getNotas());
        }

        Lote saved = loteRepository.save(lote);
        return toDetalleResponse(saved);
    }

    @Transactional
    public void eliminarLote(UUID loteId, String userEmail) {
        Lote lote = loteRepository.findById(loteId)
                .orElseThrow(() -> new ResourceNotFoundException("Lote no encontrado"));

        validarPropietario(lote, userEmail);
        Finca finca = lote.getFinca();
        loteRepository.delete(lote);
        recalcularAreaFinca(finca);
    }

    // ======================== Geometría ========================

    /**
     * Construye un polígono JTS a partir de las coordenadas GPS del recorrido.
     * Cierra automáticamente el polígono si el primer y último punto no coinciden.
     * Valida que el polígono resultante sea topológicamente correcto.
     */
    private Polygon construirPoligono(List<CoordenadaDTO> coordenadas) {
        if (coordenadas == null || coordenadas.size() < 3) {
            throw new InvalidPolygonException("Se necesitan al menos 3 coordenadas para formar un polígono");
        }

        // Construir array de coordenadas JTS (nota: JTS usa [lon, lat], no [lat, lon])
        List<Coordinate> jtsCoords = coordenadas.stream()
                .map(c -> new Coordinate(c.getLongitud(), c.getLatitud()))
                .collect(Collectors.toList());

        // Cerrar el polígono si el primer y último punto no coinciden
        Coordinate first = jtsCoords.get(0);
        Coordinate last = jtsCoords.get(jtsCoords.size() - 1);
        if (!first.equals2D(last)) {
            jtsCoords.add(new Coordinate(first.x, first.y));
        }

        // Asegurar mínimo de 4 puntos (3 vértices + cierre)
        if (jtsCoords.size() < 4) {
            throw new InvalidPolygonException("El polígono necesita al menos 3 vértices distintos");
        }

        try {
            Coordinate[] coordArray = jtsCoords.toArray(new Coordinate[0]);
            LinearRing ring = geometryFactory.createLinearRing(coordArray);
            Polygon polygon = geometryFactory.createPolygon(ring);

            if (!polygon.isValid()) {
                // Intentar reparar polígonos con autocruzamientos leves
                Geometry fixed = polygon.buffer(0);
                if (fixed instanceof Polygon && fixed.isValid()) {
                    polygon = (Polygon) fixed;
                    logger.warn("Polígono reparado automáticamente mediante buffer(0)");
                } else {
                    throw new InvalidPolygonException(
                            "El polígono formado por las coordenadas GPS es inválido (posible autocruzamiento). " +
                            "Intenta recorrer el lote nuevamente de forma más uniforme."
                    );
                }
            }

            polygon.setSRID(4326);
            return polygon;

        } catch (IllegalArgumentException e) {
            throw new InvalidPolygonException("Error al construir el polígono: " + e.getMessage());
        }
    }

    /**
     * Extrae las coordenadas de un polígono JTS para enviarlas como DTOs al frontend.
     * Excluye el punto de cierre duplicado.
     */
    private List<CoordenadaDTO> extraerCoordenadas(Polygon polygon) {
        if (polygon == null) return List.of();

        Coordinate[] coords = polygon.getExteriorRing().getCoordinates();
        List<CoordenadaDTO> result = new ArrayList<>();

        // Omitir el último punto que es la repetición del primero (cierre)
        for (int i = 0; i < coords.length - 1; i++) {
            result.add(new CoordenadaDTO(coords[i].y, coords[i].x)); // JTS: [lon,lat] → DTO: [lat,lon]
        }

        return result;
    }

    // ======================== Mappers ========================

    private LoteDetalleResponse toDetalleResponse(Lote lote) {
        LoteDetalleResponse response = new LoteDetalleResponse();
        response.setId(lote.getId());
        response.setNombre(lote.getNombre());
        response.setVariedadCacao(lote.getVariedadCacao());
        response.setAreaHectareas(lote.getAreaHectareas());
        response.setColorHex(lote.getColorHex());
        response.setEstado(lote.getEstado().name());
        response.setCreatedAt(lote.getCreatedAt());
        response.setFincaId(lote.getFinca().getId());
        response.setFincaNombre(lote.getFinca().getNombre());
        response.setAnoSiembra(lote.getAnoSiembra());
        response.setNumeroPlantas(lote.getNumeroPlantas());
        response.setPerimetroMetros(lote.getPerimetroMetros());
        response.setCoordenadas(extraerCoordenadas(lote.getGeometria()));
        response.setNotas(lote.getNotas());

        // Estadísticas del lote
        long totalInspecciones = inspeccionRepository.countByLoteId(lote.getId());
        long totalDetecciones = deteccionRepository.countByLoteId(lote.getId());
        response.setTotalInspecciones(totalInspecciones);
        response.setTotalDetecciones(totalDetecciones);

        // Última inspección
        Inspeccion ultimaInspeccion = inspeccionRepository.findUltimaInspeccionByLoteId(lote.getId());
        if (ultimaInspeccion != null) {
            response.setUltimaInspeccion(ultimaInspeccion.getFechaInspeccion());
        }

        // Resumen de enfermedades agrupado
        List<Object[]> resumenRaw = deteccionRepository.contarPorEnfermedadYLote(lote.getId());
        Map<String, Long> resumen = new LinkedHashMap<>();
        for (Object[] row : resumenRaw) {
            resumen.put((String) row[0], (Long) row[1]);
        }
        response.setResumenEnfermedades(resumen);

        return response;
    }

    private DeteccionGeoreferenciadaDTOs.DeteccionMarkerResponse toMarkerResponse(DeteccionGeoreferenciada d) {
        DeteccionGeoreferenciadaDTOs.DeteccionMarkerResponse marker = new DeteccionGeoreferenciadaDTOs.DeteccionMarkerResponse();
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

    private void recalcularAreaFinca(Finca finca) {
        BigDecimal areaTotal = finca.getLotes().stream()
                .filter(l -> l.getAreaHectareas() != null)
                .map(Lote::getAreaHectareas)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        finca.setAreaTotalHectareas(areaTotal);
        fincaRepository.save(finca);
    }

    private void validarPropietario(Lote lote, String userEmail) {
        if (!lote.getFinca().getUsuario().getEmail().equals(userEmail)) {
            throw new IllegalArgumentException("No tienes permisos sobre este lote");
        }
    }
}
