package com.cacaoscan.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTOs para el módulo de Fincas.
 */
public class FincaDTOs {

    // ======================== Request ========================

    public static class CrearFincaRequest {

        @NotBlank(message = "El nombre de la finca es obligatorio")
        @Size(max = 200, message = "El nombre no puede exceder 200 caracteres")
        private String nombre;

        @Size(max = 100)
        private String departamento;

        @Size(max = 100)
        private String municipio;

        @Size(max = 150)
        private String vereda;

        private String descripcion;

        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }

        public String getDepartamento() { return departamento; }
        public void setDepartamento(String departamento) { this.departamento = departamento; }

        public String getMunicipio() { return municipio; }
        public void setMunicipio(String municipio) { this.municipio = municipio; }

        public String getVereda() { return vereda; }
        public void setVereda(String vereda) { this.vereda = vereda; }

        public String getDescripcion() { return descripcion; }
        public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    }

    // ======================== Response ========================

    public static class FincaResponse {
        private UUID id;
        private String nombre;
        private String departamento;
        private String municipio;
        private String vereda;
        private BigDecimal areaTotalHectareas;
        private String descripcion;
        private boolean activa;
        private int totalLotes;
        private LocalDateTime createdAt;

        public FincaResponse() {}

        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }

        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }

        public String getDepartamento() { return departamento; }
        public void setDepartamento(String departamento) { this.departamento = departamento; }

        public String getMunicipio() { return municipio; }
        public void setMunicipio(String municipio) { this.municipio = municipio; }

        public String getVereda() { return vereda; }
        public void setVereda(String vereda) { this.vereda = vereda; }

        public BigDecimal getAreaTotalHectareas() { return areaTotalHectareas; }
        public void setAreaTotalHectareas(BigDecimal areaTotalHectareas) { this.areaTotalHectareas = areaTotalHectareas; }

        public String getDescripcion() { return descripcion; }
        public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

        public boolean isActiva() { return activa; }
        public void setActiva(boolean activa) { this.activa = activa; }

        public int getTotalLotes() { return totalLotes; }
        public void setTotalLotes(int totalLotes) { this.totalLotes = totalLotes; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }

    public static class FincaDetalleResponse extends FincaResponse {
        private List<LoteDTOs.LoteResumenResponse> lotes;

        public List<LoteDTOs.LoteResumenResponse> getLotes() { return lotes; }
        public void setLotes(List<LoteDTOs.LoteResumenResponse> lotes) { this.lotes = lotes; }
    }
}
