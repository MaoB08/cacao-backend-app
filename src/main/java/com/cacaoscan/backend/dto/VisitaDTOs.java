package com.cacaoscan.backend.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTOs para el módulo de visitas técnicas del agrónomo.
 */
public class VisitaDTOs {

    // ======================== Requests ========================

    public static class CrearVisitaRequest {

        @NotNull(message = "La finca es obligatoria")
        private UUID fincaId;

        private UUID loteId;

        @NotNull(message = "La fecha programada es obligatoria")
        @Future(message = "La fecha debe ser futura")
        private LocalDateTime fechaProgramada;

        private Integer duracionEstimadaMin;

        @Size(max = 500, message = "El motivo no puede exceder 500 caracteres")
        private String motivo;

        private String notasPrevias;

        public UUID getFincaId() { return fincaId; }
        public void setFincaId(UUID fincaId) { this.fincaId = fincaId; }

        public UUID getLoteId() { return loteId; }
        public void setLoteId(UUID loteId) { this.loteId = loteId; }

        public LocalDateTime getFechaProgramada() { return fechaProgramada; }
        public void setFechaProgramada(LocalDateTime fechaProgramada) { this.fechaProgramada = fechaProgramada; }

        public Integer getDuracionEstimadaMin() { return duracionEstimadaMin; }
        public void setDuracionEstimadaMin(Integer duracionEstimadaMin) { this.duracionEstimadaMin = duracionEstimadaMin; }

        public String getMotivo() { return motivo; }
        public void setMotivo(String motivo) { this.motivo = motivo; }

        public String getNotasPrevias() { return notasPrevias; }
        public void setNotasPrevias(String notasPrevias) { this.notasPrevias = notasPrevias; }
    }

    public static class ReprogramarVisitaRequest {

        @NotNull(message = "La nueva fecha es obligatoria")
        @Future(message = "La nueva fecha debe ser futura")
        private LocalDateTime fechaProgramada;

        private Integer duracionEstimadaMin;

        @Size(max = 500)
        private String motivo;

        private String notasPrevias;

        public LocalDateTime getFechaProgramada() { return fechaProgramada; }
        public void setFechaProgramada(LocalDateTime fechaProgramada) { this.fechaProgramada = fechaProgramada; }

        public Integer getDuracionEstimadaMin() { return duracionEstimadaMin; }
        public void setDuracionEstimadaMin(Integer duracionEstimadaMin) { this.duracionEstimadaMin = duracionEstimadaMin; }

        public String getMotivo() { return motivo; }
        public void setMotivo(String motivo) { this.motivo = motivo; }

        public String getNotasPrevias() { return notasPrevias; }
        public void setNotasPrevias(String notasPrevias) { this.notasPrevias = notasPrevias; }
    }

    public static class CancelarVisitaRequest {

        @NotNull(message = "El motivo de cancelación es obligatorio")
        @Size(min = 5, max = 500, message = "El motivo debe tener entre 5 y 500 caracteres")
        private String motivoCancelacion;

        public String getMotivoCancelacion() { return motivoCancelacion; }
        public void setMotivoCancelacion(String motivoCancelacion) { this.motivoCancelacion = motivoCancelacion; }
    }

    public static class FinalizarVisitaRequest {

        private String observaciones;
        private String recomendaciones;
        private Integer plantasRevisadas;
        private Integer plantasEnfermas;
        private String informeFinal;

        public String getObservaciones() { return observaciones; }
        public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

        public String getRecomendaciones() { return recomendaciones; }
        public void setRecomendaciones(String recomendaciones) { this.recomendaciones = recomendaciones; }

        public Integer getPlantasRevisadas() { return plantasRevisadas; }
        public void setPlantasRevisadas(Integer plantasRevisadas) { this.plantasRevisadas = plantasRevisadas; }

        public Integer getPlantasEnfermas() { return plantasEnfermas; }
        public void setPlantasEnfermas(Integer plantasEnfermas) { this.plantasEnfermas = plantasEnfermas; }

        public String getInformeFinal() { return informeFinal; }
        public void setInformeFinal(String informeFinal) { this.informeFinal = informeFinal; }
    }

    // ======================== Responses ========================

    /**
     * Resumen de visita para listas y tarjetas.
     */
    public static class VisitaResumenResponse {
        private UUID id;
        private String estado;
        private LocalDateTime fechaProgramada;
        private Integer duracionEstimadaMin;
        private String motivo;

        // Finca
        private UUID fincaId;
        private String fincaNombre;
        private String fincaMunicipio;

        // Agricultor
        private String agricultorNombre;

        // Lote (opcional)
        private UUID loteId;
        private String loteNombre;
        private String loteVariedadCacao;

        private LocalDateTime createdAt;

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

        public String getAgricultorNombre() { return agricultorNombre; }
        public void setAgricultorNombre(String agricultorNombre) { this.agricultorNombre = agricultorNombre; }

        public UUID getLoteId() { return loteId; }
        public void setLoteId(UUID loteId) { this.loteId = loteId; }

        public String getLoteNombre() { return loteNombre; }
        public void setLoteNombre(String loteNombre) { this.loteNombre = loteNombre; }

        public String getLoteVariedadCacao() { return loteVariedadCacao; }
        public void setLoteVariedadCacao(String loteVariedadCacao) { this.loteVariedadCacao = loteVariedadCacao; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }

    /**
     * Detalle completo de una visita, incluye datos de ejecución y resultados.
     */
    public static class VisitaDetalleResponse extends VisitaResumenResponse {

        private String notasPrevias;

        // Agricultor extendido
        private UUID agricultorId;
        private String agricultorTelefono;

        // Finca extendida
        private String fincaDepartamento;
        private String fincaVereda;

        // Ejecución
        private LocalDateTime fechaInicioReal;
        private LocalDateTime fechaFinReal;
        private Integer duracionRealMin;

        // Resultados
        private String observaciones;
        private String recomendaciones;
        private Integer plantasRevisadas;
        private Integer plantasEnfermas;
        private String informeFinal;

        // Cancelación
        private String motivoCancelacion;

        // Imágenes
        private int totalImagenes;
        private List<VisitaImagenResponse> imagenes;

        private LocalDateTime updatedAt;

        public String getNotasPrevias() { return notasPrevias; }
        public void setNotasPrevias(String notasPrevias) { this.notasPrevias = notasPrevias; }

        public UUID getAgricultorId() { return agricultorId; }
        public void setAgricultorId(UUID agricultorId) { this.agricultorId = agricultorId; }

        public String getAgricultorTelefono() { return agricultorTelefono; }
        public void setAgricultorTelefono(String agricultorTelefono) { this.agricultorTelefono = agricultorTelefono; }

        public String getFincaDepartamento() { return fincaDepartamento; }
        public void setFincaDepartamento(String fincaDepartamento) { this.fincaDepartamento = fincaDepartamento; }

        public String getFincaVereda() { return fincaVereda; }
        public void setFincaVereda(String fincaVereda) { this.fincaVereda = fincaVereda; }

        public LocalDateTime getFechaInicioReal() { return fechaInicioReal; }
        public void setFechaInicioReal(LocalDateTime fechaInicioReal) { this.fechaInicioReal = fechaInicioReal; }

        public LocalDateTime getFechaFinReal() { return fechaFinReal; }
        public void setFechaFinReal(LocalDateTime fechaFinReal) { this.fechaFinReal = fechaFinReal; }

        public Integer getDuracionRealMin() { return duracionRealMin; }
        public void setDuracionRealMin(Integer duracionRealMin) { this.duracionRealMin = duracionRealMin; }

        public String getObservaciones() { return observaciones; }
        public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

        public String getRecomendaciones() { return recomendaciones; }
        public void setRecomendaciones(String recomendaciones) { this.recomendaciones = recomendaciones; }

        public Integer getPlantasRevisadas() { return plantasRevisadas; }
        public void setPlantasRevisadas(Integer plantasRevisadas) { this.plantasRevisadas = plantasRevisadas; }

        public Integer getPlantasEnfermas() { return plantasEnfermas; }
        public void setPlantasEnfermas(Integer plantasEnfermas) { this.plantasEnfermas = plantasEnfermas; }

        public String getInformeFinal() { return informeFinal; }
        public void setInformeFinal(String informeFinal) { this.informeFinal = informeFinal; }

        public String getMotivoCancelacion() { return motivoCancelacion; }
        public void setMotivoCancelacion(String motivoCancelacion) { this.motivoCancelacion = motivoCancelacion; }

        public int getTotalImagenes() { return totalImagenes; }
        public void setTotalImagenes(int totalImagenes) { this.totalImagenes = totalImagenes; }

        public List<VisitaImagenResponse> getImagenes() { return imagenes; }
        public void setImagenes(List<VisitaImagenResponse> imagenes) { this.imagenes = imagenes; }

        public LocalDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    }

    /**
     * Imagen vinculada a una visita.
     */
    public static class VisitaImagenResponse {
        private UUID id;
        private String imagenUrl;
        private String notas;
        private UUID loteId;
        private String loteNombre;
        private UUID analisisId;
        private String enfermedadDetectada;
        private String confianza;
        private Boolean tieneEnfermedad;
        private LocalDateTime createdAt;

        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }

        public String getImagenUrl() { return imagenUrl; }
        public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }

        public String getNotas() { return notas; }
        public void setNotas(String notas) { this.notas = notas; }

        public UUID getLoteId() { return loteId; }
        public void setLoteId(UUID loteId) { this.loteId = loteId; }

        public String getLoteNombre() { return loteNombre; }
        public void setLoteNombre(String loteNombre) { this.loteNombre = loteNombre; }

        public UUID getAnalisisId() { return analisisId; }
        public void setAnalisisId(UUID analisisId) { this.analisisId = analisisId; }

        public String getEnfermedadDetectada() { return enfermedadDetectada; }
        public void setEnfermedadDetectada(String enfermedadDetectada) { this.enfermedadDetectada = enfermedadDetectada; }

        public String getConfianza() { return confianza; }
        public void setConfianza(String confianza) { this.confianza = confianza; }

        public Boolean getTieneEnfermedad() { return tieneEnfermedad; }
        public void setTieneEnfermedad(Boolean tieneEnfermedad) { this.tieneEnfermedad = tieneEnfermedad; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }

    /**
     * Respuesta del análisis IA de imagen vinculada a una visita técnica.
     */
    public static class AnalisisImagenResponse {
        private String status; // "ok", "no_disponible", "no_concluyente", "error"
        private String mensaje;
        private UUID imagenId;
        private UUID analisisId;
        private String enfermedadDetectada;
        private String nombreCientifico;
        private String confianza;
        private String severidad;
        private String modelVersion;
        private LocalDateTime fechaAnalisis;

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getMensaje() { return mensaje; }
        public void setMensaje(String mensaje) { this.mensaje = mensaje; }

        public UUID getImagenId() { return imagenId; }
        public void setImagenId(UUID imagenId) { this.imagenId = imagenId; }

        public UUID getAnalisisId() { return analisisId; }
        public void setAnalisisId(UUID analisisId) { this.analisisId = analisisId; }

        public String getEnfermedadDetectada() { return enfermedadDetectada; }
        public void setEnfermedadDetectada(String enfermedadDetectada) { this.enfermedadDetectada = enfermedadDetectada; }

        public String getNombreCientifico() { return nombreCientifico; }
        public void setNombreCientifico(String nombreCientifico) { this.nombreCientifico = nombreCientifico; }

        public String getConfianza() { return confianza; }
        public void setConfianza(String confianza) { this.confianza = confianza; }

        public String getSeveridad() { return severidad; }
        public void setSeveridad(String severidad) { this.severidad = severidad; }

        public String getModelVersion() { return modelVersion; }
        public void setModelVersion(String modelVersion) { this.modelVersion = modelVersion; }

        public LocalDateTime getFechaAnalisis() { return fechaAnalisis; }
        public void setFechaAnalisis(LocalDateTime fechaAnalisis) { this.fechaAnalisis = fechaAnalisis; }
    }
}
