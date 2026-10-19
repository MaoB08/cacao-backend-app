package com.cacaoscan.backend.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTOs para la vista de visitas del agricultor.
 * Solo lectura. Excluye notas internas del agrónomo (notasPrevias, observaciones internas).
 */
public class AgricultorVisitaDTOs {

    /**
     * Resumen de visita para lista del agricultor.
     */
    public static class VisitaAgricultorResumen {
        private UUID id;
        private String estado;
        private LocalDateTime fechaProgramada;
        private Integer duracionEstimadaMin;
        private String motivo;

        // Finca
        private UUID fincaId;
        private String fincaNombre;
        private String fincaMunicipio;

        // Agrónomo (nombre profesional, no datos internos)
        private String agronomoNombre;

        // Lote (opcional)
        private UUID loteId;
        private String loteNombre;

        // Ejecución (solo si completada)
        private LocalDateTime fechaInicioReal;
        private LocalDateTime fechaFinReal;
        private Integer duracionRealMin;

        // Recomendaciones compartidas (NO observaciones internas)
        private String recomendaciones;
        private Integer plantasRevisadas;
        private Integer plantasEnfermas;

        private LocalDateTime createdAt;

        // Getters y Setters
        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }

        public String getEstado() { return estado; }
        public void setEstado(String estado) { this.estado = estado; }

        public LocalDateTime getFechaProgramada() { return fechaProgramada; }
        public void setFechaProgramada(LocalDateTime fechaProgramada) { this.fechaProgramada = fechaProgramada; }

        public Integer getDuracionEstimadaMin() { return duracionEstimadaMin; }
        public void setDuracionEstimadaMin(Integer duracionEstimadaMin) { this.duracionEstimadaMin = duracionEstimadaMin; }

        public String getMotivo() { return motivo; }
        public void setMotivo(String motivo) { this.motivo = motivo; }

        public UUID getFincaId() { return fincaId; }
        public void setFincaId(UUID fincaId) { this.fincaId = fincaId; }

        public String getFincaNombre() { return fincaNombre; }
        public void setFincaNombre(String fincaNombre) { this.fincaNombre = fincaNombre; }

        public String getFincaMunicipio() { return fincaMunicipio; }
        public void setFincaMunicipio(String fincaMunicipio) { this.fincaMunicipio = fincaMunicipio; }

        public String getAgronomoNombre() { return agronomoNombre; }
        public void setAgronomoNombre(String agronomoNombre) { this.agronomoNombre = agronomoNombre; }

        public UUID getLoteId() { return loteId; }
        public void setLoteId(UUID loteId) { this.loteId = loteId; }

        public String getLoteNombre() { return loteNombre; }
        public void setLoteNombre(String loteNombre) { this.loteNombre = loteNombre; }

        public LocalDateTime getFechaInicioReal() { return fechaInicioReal; }
        public void setFechaInicioReal(LocalDateTime fechaInicioReal) { this.fechaInicioReal = fechaInicioReal; }

        public LocalDateTime getFechaFinReal() { return fechaFinReal; }
        public void setFechaFinReal(LocalDateTime fechaFinReal) { this.fechaFinReal = fechaFinReal; }

        public Integer getDuracionRealMin() { return duracionRealMin; }
        public void setDuracionRealMin(Integer duracionRealMin) { this.duracionRealMin = duracionRealMin; }

        public String getRecomendaciones() { return recomendaciones; }
        public void setRecomendaciones(String recomendaciones) { this.recomendaciones = recomendaciones; }

        public Integer getPlantasRevisadas() { return plantasRevisadas; }
        public void setPlantasRevisadas(Integer plantasRevisadas) { this.plantasRevisadas = plantasRevisadas; }

        public Integer getPlantasEnfermas() { return plantasEnfermas; }
        public void setPlantasEnfermas(Integer plantasEnfermas) { this.plantasEnfermas = plantasEnfermas; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }

    /**
     * Resumen de visitas para el dashboard del agricultor.
     */
    public static class VisitasAgricultorDashboardInfo {
        private long proximasVisitas;
        private long completadasTotal;
        private List<ProximaVisitaAgricultorInfo> proximas;

        public VisitasAgricultorDashboardInfo(long proximasVisitas, long completadasTotal,
                                               List<ProximaVisitaAgricultorInfo> proximas) {
            this.proximasVisitas = proximasVisitas;
            this.completadasTotal = completadasTotal;
            this.proximas = proximas;
        }

        public long getProximasVisitas() { return proximasVisitas; }
        public void setProximasVisitas(long proximasVisitas) { this.proximasVisitas = proximasVisitas; }

        public long getCompletadasTotal() { return completadasTotal; }
        public void setCompletadasTotal(long completadasTotal) { this.completadasTotal = completadasTotal; }

        public List<ProximaVisitaAgricultorInfo> getProximas() { return proximas; }
        public void setProximas(List<ProximaVisitaAgricultorInfo> proximas) { this.proximas = proximas; }
    }

    /**
     * Info resumida de una próxima visita para el dashboard del agricultor.
     */
    public static class ProximaVisitaAgricultorInfo {
        private UUID id;
        private String estado;
        private LocalDateTime fechaProgramada;
        private String fincaNombre;
        private String agronomoNombre;
        private String motivo;
        private String loteNombre;

        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }

        public String getEstado() { return estado; }
        public void setEstado(String estado) { this.estado = estado; }

        public LocalDateTime getFechaProgramada() { return fechaProgramada; }
        public void setFechaProgramada(LocalDateTime fechaProgramada) { this.fechaProgramada = fechaProgramada; }

        public String getFincaNombre() { return fincaNombre; }
        public void setFincaNombre(String fincaNombre) { this.fincaNombre = fincaNombre; }

        public String getAgronomoNombre() { return agronomoNombre; }
        public void setAgronomoNombre(String agronomoNombre) { this.agronomoNombre = agronomoNombre; }

        public String getMotivo() { return motivo; }
        public void setMotivo(String motivo) { this.motivo = motivo; }

        public String getLoteNombre() { return loteNombre; }
        public void setLoteNombre(String loteNombre) { this.loteNombre = loteNombre; }
    }
}
