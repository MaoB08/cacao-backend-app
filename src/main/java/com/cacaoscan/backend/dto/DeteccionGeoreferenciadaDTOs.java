package com.cacaoscan.backend.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTOs para Detecciones Georreferenciadas de enfermedades del cacao.
 */
public class DeteccionGeoreferenciadaDTOs {

    // ======================== Request ========================

    public static class CrearDeteccionRequest {

        private UUID inspeccionId;

        @NotBlank(message = "El nombre de la enfermedad es obligatorio")
        @Size(max = 150)
        private String enfermedad;

        @NotNull(message = "El porcentaje de confianza es obligatorio")
        @DecimalMin(value = "0.0", message = "La confianza mínima es 0%")
        @DecimalMax(value = "100.0", message = "La confianza máxima es 100%")
        private BigDecimal porcentajeConfianza;

        @Size(max = 50)
        private String severidad;

        private String imagenUrl;

        @NotNull(message = "La latitud es obligatoria")
        private Double latitud;

        @NotNull(message = "La longitud es obligatoria")
        private Double longitud;

        private String observaciones;

        public UUID getInspeccionId() { return inspeccionId; }
        public void setInspeccionId(UUID inspeccionId) { this.inspeccionId = inspeccionId; }

        public String getEnfermedad() { return enfermedad; }
        public void setEnfermedad(String enfermedad) { this.enfermedad = enfermedad; }

        public BigDecimal getPorcentajeConfianza() { return porcentajeConfianza; }
        public void setPorcentajeConfianza(BigDecimal porcentajeConfianza) { this.porcentajeConfianza = porcentajeConfianza; }

        public String getSeveridad() { return severidad; }
        public void setSeveridad(String severidad) { this.severidad = severidad; }

        public String getImagenUrl() { return imagenUrl; }
        public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }

        public Double getLatitud() { return latitud; }
        public void setLatitud(Double latitud) { this.latitud = latitud; }

        public Double getLongitud() { return longitud; }
        public void setLongitud(Double longitud) { this.longitud = longitud; }

        public String getObservaciones() { return observaciones; }
        public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    }

    // ======================== Responses ========================

    /**
     * Respuesta completa de una detección (para listados y detalle).
     */
    public static class DeteccionResponse {
        private UUID id;
        private UUID loteId;
        private String loteNombre;
        private UUID inspeccionId;
        private String enfermedad;
        private BigDecimal porcentajeConfianza;
        private String severidad;
        private String imagenUrl;
        private Double latitud;
        private Double longitud;
        private String observaciones;
        private LocalDateTime fechaDeteccion;

        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }

        public UUID getLoteId() { return loteId; }
        public void setLoteId(UUID loteId) { this.loteId = loteId; }

        public String getLoteNombre() { return loteNombre; }
        public void setLoteNombre(String loteNombre) { this.loteNombre = loteNombre; }

        public UUID getInspeccionId() { return inspeccionId; }
        public void setInspeccionId(UUID inspeccionId) { this.inspeccionId = inspeccionId; }

        public String getEnfermedad() { return enfermedad; }
        public void setEnfermedad(String enfermedad) { this.enfermedad = enfermedad; }

        public BigDecimal getPorcentajeConfianza() { return porcentajeConfianza; }
        public void setPorcentajeConfianza(BigDecimal porcentajeConfianza) { this.porcentajeConfianza = porcentajeConfianza; }

        public String getSeveridad() { return severidad; }
        public void setSeveridad(String severidad) { this.severidad = severidad; }

        public String getImagenUrl() { return imagenUrl; }
        public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }

        public Double getLatitud() { return latitud; }
        public void setLatitud(Double latitud) { this.latitud = latitud; }

        public Double getLongitud() { return longitud; }
        public void setLongitud(Double longitud) { this.longitud = longitud; }

        public String getObservaciones() { return observaciones; }
        public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

        public LocalDateTime getFechaDeteccion() { return fechaDeteccion; }
        public void setFechaDeteccion(LocalDateTime fechaDeteccion) { this.fechaDeteccion = fechaDeteccion; }
    }

    /**
     * Respuesta ligera para renderizar Markers en el mapa.
     * Solo contiene lat/lng, enfermedad y color — optimizada para bajo payload.
     */
    public static class DeteccionMarkerResponse {
        private UUID id;
        private String enfermedad;
        private BigDecimal porcentajeConfianza;
        private String severidad;
        private Double latitud;
        private Double longitud;
        private LocalDateTime fechaDeteccion;

        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }

        public String getEnfermedad() { return enfermedad; }
        public void setEnfermedad(String enfermedad) { this.enfermedad = enfermedad; }

        public BigDecimal getPorcentajeConfianza() { return porcentajeConfianza; }
        public void setPorcentajeConfianza(BigDecimal porcentajeConfianza) { this.porcentajeConfianza = porcentajeConfianza; }

        public String getSeveridad() { return severidad; }
        public void setSeveridad(String severidad) { this.severidad = severidad; }

        public Double getLatitud() { return latitud; }
        public void setLatitud(Double latitud) { this.latitud = latitud; }

        public Double getLongitud() { return longitud; }
        public void setLongitud(Double longitud) { this.longitud = longitud; }

        public LocalDateTime getFechaDeteccion() { return fechaDeteccion; }
        public void setFechaDeteccion(LocalDateTime fechaDeteccion) { this.fechaDeteccion = fechaDeteccion; }
    }
}
