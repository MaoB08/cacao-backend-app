package com.cacaoscan.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * DTOs para el módulo de Lotes Agrícolas.
 */
public class LoteDTOs {

    // ======================== Coordenada ========================

    /**
     * Representa un punto geográfico lat/lng enviado desde el GPS del dispositivo móvil.
     */
    public static class CoordenadaDTO {
        @NotNull(message = "La latitud es obligatoria")
        private Double latitud;

        @NotNull(message = "La longitud es obligatoria")
        private Double longitud;

        public CoordenadaDTO() {}

        public CoordenadaDTO(Double latitud, Double longitud) {
            this.latitud = latitud;
            this.longitud = longitud;
        }

        public Double getLatitud() { return latitud; }
        public void setLatitud(Double latitud) { this.latitud = latitud; }

        public Double getLongitud() { return longitud; }
        public void setLongitud(Double longitud) { this.longitud = longitud; }
    }

    // ======================== Request ========================

    public static class CrearLoteRequest {

        @NotNull(message = "El ID de la finca es obligatorio")
        private UUID fincaId;

        @NotBlank(message = "El nombre del lote es obligatorio")
        @Size(max = 200, message = "El nombre no puede exceder 200 caracteres")
        private String nombre;

        @Size(max = 100)
        private String variedadCacao;

        private Integer anoSiembra;

        private Integer numeroPlantas;

        /**
         * Lista de coordenadas GPS capturadas durante el recorrido del agricultor.
         * Debe tener al menos 3 puntos distintos para formar un polígono válido.
         * El backend cierra automáticamente el polígono si el primer y último punto no coinciden.
         */
        @NotNull(message = "Las coordenadas del recorrido son obligatorias")
        @Size(min = 3, message = "Se necesitan al menos 3 puntos para formar un polígono")
        private List<CoordenadaDTO> coordenadas;

        @Size(max = 9)
        private String colorHex;

        private String notas;

        public UUID getFincaId() { return fincaId; }
        public void setFincaId(UUID fincaId) { this.fincaId = fincaId; }

        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }

        public String getVariedadCacao() { return variedadCacao; }
        public void setVariedadCacao(String variedadCacao) { this.variedadCacao = variedadCacao; }

        public Integer getAnoSiembra() { return anoSiembra; }
        public void setAnoSiembra(Integer anoSiembra) { this.anoSiembra = anoSiembra; }

        public Integer getNumeroPlantas() { return numeroPlantas; }
        public void setNumeroPlantas(Integer numeroPlantas) { this.numeroPlantas = numeroPlantas; }

        public List<CoordenadaDTO> getCoordenadas() { return coordenadas; }
        public void setCoordenadas(List<CoordenadaDTO> coordenadas) { this.coordenadas = coordenadas; }

        public String getColorHex() { return colorHex; }
        public void setColorHex(String colorHex) { this.colorHex = colorHex; }

        public String getNotas() { return notas; }
        public void setNotas(String notas) { this.notas = notas; }
    }

    public static class ActualizarLoteRequest {

        @Size(max = 200)
        private String nombre;

        @Size(max = 100)
        private String variedadCacao;

        private Integer anoSiembra;

        private Integer numeroPlantas;

        @Size(max = 9)
        private String colorHex;

        private String estado;

        private String notas;

        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }

        public String getVariedadCacao() { return variedadCacao; }
        public void setVariedadCacao(String variedadCacao) { this.variedadCacao = variedadCacao; }

        public Integer getAnoSiembra() { return anoSiembra; }
        public void setAnoSiembra(Integer anoSiembra) { this.anoSiembra = anoSiembra; }

        public Integer getNumeroPlantas() { return numeroPlantas; }
        public void setNumeroPlantas(Integer numeroPlantas) { this.numeroPlantas = numeroPlantas; }

        public String getColorHex() { return colorHex; }
        public void setColorHex(String colorHex) { this.colorHex = colorHex; }

        public String getEstado() { return estado; }
        public void setEstado(String estado) { this.estado = estado; }

        public String getNotas() { return notas; }
        public void setNotas(String notas) { this.notas = notas; }
    }

    // ======================== Responses ========================

    /**
     * Respuesta resumida del lote, usada en listados.
     */
    public static class LoteResumenResponse {
        private UUID id;
        private String nombre;
        private String variedadCacao;
        private BigDecimal areaHectareas;
        private String colorHex;
        private String estado;
        private long totalInspecciones;
        private long totalDetecciones;
        private LocalDateTime createdAt;

        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }

        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }

        public String getVariedadCacao() { return variedadCacao; }
        public void setVariedadCacao(String variedadCacao) { this.variedadCacao = variedadCacao; }

        public BigDecimal getAreaHectareas() { return areaHectareas; }
        public void setAreaHectareas(BigDecimal areaHectareas) { this.areaHectareas = areaHectareas; }

        public String getColorHex() { return colorHex; }
        public void setColorHex(String colorHex) { this.colorHex = colorHex; }

        public String getEstado() { return estado; }
        public void setEstado(String estado) { this.estado = estado; }

        public long getTotalInspecciones() { return totalInspecciones; }
        public void setTotalInspecciones(long totalInspecciones) { this.totalInspecciones = totalInspecciones; }

        public long getTotalDetecciones() { return totalDetecciones; }
        public void setTotalDetecciones(long totalDetecciones) { this.totalDetecciones = totalDetecciones; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }

    /**
     * Respuesta completa del lote con su polígono y estadísticas.
     * Incluye coordenadas del polígono para renderizar en react-native-maps.
     */
    public static class LoteDetalleResponse extends LoteResumenResponse {
        private UUID fincaId;
        private String fincaNombre;
        private Integer anoSiembra;
        private Integer numeroPlantas;
        private BigDecimal perimetroMetros;
        private List<CoordenadaDTO> coordenadas;
        private String notas;
        private LocalDateTime ultimaInspeccion;
        private Map<String, Long> resumenEnfermedades;

        public UUID getFincaId() { return fincaId; }
        public void setFincaId(UUID fincaId) { this.fincaId = fincaId; }

        public String getFincaNombre() { return fincaNombre; }
        public void setFincaNombre(String fincaNombre) { this.fincaNombre = fincaNombre; }

        public Integer getAnoSiembra() { return anoSiembra; }
        public void setAnoSiembra(Integer anoSiembra) { this.anoSiembra = anoSiembra; }

        public Integer getNumeroPlantas() { return numeroPlantas; }
        public void setNumeroPlantas(Integer numeroPlantas) { this.numeroPlantas = numeroPlantas; }

        public BigDecimal getPerimetroMetros() { return perimetroMetros; }
        public void setPerimetroMetros(BigDecimal perimetroMetros) { this.perimetroMetros = perimetroMetros; }

        public List<CoordenadaDTO> getCoordenadas() { return coordenadas; }
        public void setCoordenadas(List<CoordenadaDTO> coordenadas) { this.coordenadas = coordenadas; }

        public String getNotas() { return notas; }
        public void setNotas(String notas) { this.notas = notas; }

        public LocalDateTime getUltimaInspeccion() { return ultimaInspeccion; }
        public void setUltimaInspeccion(LocalDateTime ultimaInspeccion) { this.ultimaInspeccion = ultimaInspeccion; }

        public Map<String, Long> getResumenEnfermedades() { return resumenEnfermedades; }
        public void setResumenEnfermedades(Map<String, Long> resumenEnfermedades) { this.resumenEnfermedades = resumenEnfermedades; }
    }

    /**
     * Respuesta para la vista del mapa: incluye coordenadas del polígono
     * y marcadores de detecciones para renderizar todo en una sola carga.
     */
    public static class LoteMapaResponse {
        private UUID id;
        private String nombre;
        private BigDecimal areaHectareas;
        private String colorHex;
        private String estado;
        private List<CoordenadaDTO> coordenadas;
        private List<DeteccionGeoreferenciadaDTOs.DeteccionMarkerResponse> detecciones;

        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }

        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }

        public BigDecimal getAreaHectareas() { return areaHectareas; }
        public void setAreaHectareas(BigDecimal areaHectareas) { this.areaHectareas = areaHectareas; }

        public String getColorHex() { return colorHex; }
        public void setColorHex(String colorHex) { this.colorHex = colorHex; }

        public String getEstado() { return estado; }
        public void setEstado(String estado) { this.estado = estado; }

        public List<CoordenadaDTO> getCoordenadas() { return coordenadas; }
        public void setCoordenadas(List<CoordenadaDTO> coordenadas) { this.coordenadas = coordenadas; }

        public List<DeteccionGeoreferenciadaDTOs.DeteccionMarkerResponse> getDetecciones() { return detecciones; }
        public void setDetecciones(List<DeteccionGeoreferenciadaDTOs.DeteccionMarkerResponse> detecciones) { this.detecciones = detecciones; }
    }
}
