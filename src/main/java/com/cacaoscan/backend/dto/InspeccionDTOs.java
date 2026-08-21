package com.cacaoscan.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTOs para el módulo de Inspecciones de campo.
 */
public class InspeccionDTOs {

    // ======================== Request ========================

    public static class CrearInspeccionRequest {

        @NotNull(message = "El ID del lote es obligatorio")
        private UUID loteId;

        @Min(value = 0, message = "Las plantas inspeccionadas deben ser >= 0")
        private int plantasInspeccionadas;

        @Min(value = 0, message = "Las plantas enfermas deben ser >= 0")
        private int plantasEnfermas;

        private String observaciones;

        private Integer duracionMinutos;

        public UUID getLoteId() { return loteId; }
        public void setLoteId(UUID loteId) { this.loteId = loteId; }

        public int getPlantasInspeccionadas() { return plantasInspeccionadas; }
        public void setPlantasInspeccionadas(int plantasInspeccionadas) { this.plantasInspeccionadas = plantasInspeccionadas; }

        public int getPlantasEnfermas() { return plantasEnfermas; }
        public void setPlantasEnfermas(int plantasEnfermas) { this.plantasEnfermas = plantasEnfermas; }

        public String getObservaciones() { return observaciones; }
        public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

        public Integer getDuracionMinutos() { return duracionMinutos; }
        public void setDuracionMinutos(Integer duracionMinutos) { this.duracionMinutos = duracionMinutos; }
    }

    // ======================== Response ========================

    public static class InspeccionResponse {
        private UUID id;
        private UUID loteId;
        private String loteNombre;
        private String usuarioNombre;
        private LocalDateTime fechaInspeccion;
        private int plantasInspeccionadas;
        private int plantasEnfermas;
        private int totalEnfermedades;
        private String observaciones;
        private String estado;
        private Integer duracionMinutos;
        private LocalDateTime createdAt;

        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }

        public UUID getLoteId() { return loteId; }
        public void setLoteId(UUID loteId) { this.loteId = loteId; }

        public String getLoteNombre() { return loteNombre; }
        public void setLoteNombre(String loteNombre) { this.loteNombre = loteNombre; }

        public String getUsuarioNombre() { return usuarioNombre; }
        public void setUsuarioNombre(String usuarioNombre) { this.usuarioNombre = usuarioNombre; }

        public LocalDateTime getFechaInspeccion() { return fechaInspeccion; }
        public void setFechaInspeccion(LocalDateTime fechaInspeccion) { this.fechaInspeccion = fechaInspeccion; }

        public int getPlantasInspeccionadas() { return plantasInspeccionadas; }
        public void setPlantasInspeccionadas(int plantasInspeccionadas) { this.plantasInspeccionadas = plantasInspeccionadas; }

        public int getPlantasEnfermas() { return plantasEnfermas; }
        public void setPlantasEnfermas(int plantasEnfermas) { this.plantasEnfermas = plantasEnfermas; }

        public int getTotalEnfermedades() { return totalEnfermedades; }
        public void setTotalEnfermedades(int totalEnfermedades) { this.totalEnfermedades = totalEnfermedades; }

        public String getObservaciones() { return observaciones; }
        public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

        public String getEstado() { return estado; }
        public void setEstado(String estado) { this.estado = estado; }

        public Integer getDuracionMinutos() { return duracionMinutos; }
        public void setDuracionMinutos(Integer duracionMinutos) { this.duracionMinutos = duracionMinutos; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }

    public static class InspeccionDetalleResponse extends InspeccionResponse {
        private List<DeteccionGeoreferenciadaDTOs.DeteccionResponse> detecciones;

        public List<DeteccionGeoreferenciadaDTOs.DeteccionResponse> getDetecciones() { return detecciones; }
        public void setDetecciones(List<DeteccionGeoreferenciadaDTOs.DeteccionResponse> detecciones) { this.detecciones = detecciones; }
    }
}
