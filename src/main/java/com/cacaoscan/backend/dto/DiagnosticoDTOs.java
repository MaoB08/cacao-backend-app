package com.cacaoscan.backend.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DiagnosticoDTOs {

    public static class RecomendacionItem {
        private String id;
        private String tipo; // "rojo", "naranja", "cafe", "verde"
        private String titulo;
        private String descripcion;

        public RecomendacionItem() {}

        public RecomendacionItem(String id, String tipo, String titulo, String descripcion) {
            this.id = id;
            this.tipo = tipo;
            this.titulo = titulo;
            this.descripcion = descripcion;
        }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getTipo() { return tipo; }
        public void setTipo(String tipo) { this.tipo = tipo; }

        public String getTitulo() { return titulo; }
        public void setTitulo(String titulo) { this.titulo = titulo; }

        public String getDescripcion() { return descripcion; }
        public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    }

    public static class DiagnosticoResponse {
        private UUID id;
        private String imagenUrl;
        private boolean tieneEnfermedad;
        private String nombreEnfermedad;
        private String nombreCientifico;
        private String codigoClase;          // "healthy", "monilia_m1", etc.
        private double probabilidad;          // valor numérico [0.0, 1.0]
        private String confianza;            // Presentación: "87.5%"
        private String severidad;
        private String modelVersion;          // "monilia-efficientnet_b0-0.1.0"
        private boolean concluyente;          // true si status == "ok"
        private List<RecomendacionItem> recomendaciones;
        private LocalDateTime createdAt;

        public DiagnosticoResponse() {}

        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }

        public String getImagenUrl() { return imagenUrl; }
        public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }

        public boolean isTieneEnfermedad() { return tieneEnfermedad; }
        public void setTieneEnfermedad(boolean tieneEnfermedad) { this.tieneEnfermedad = tieneEnfermedad; }

        public String getNombreEnfermedad() { return nombreEnfermedad; }
        public void setNombreEnfermedad(String nombreEnfermedad) { this.nombreEnfermedad = nombreEnfermedad; }

        public String getNombreCientifico() { return nombreCientifico; }
        public void setNombreCientifico(String nombreCientifico) { this.nombreCientifico = nombreCientifico; }

        public String getCodigoClase() { return codigoClase; }
        public void setCodigoClase(String codigoClase) { this.codigoClase = codigoClase; }

        public double getProbabilidad() { return probabilidad; }
        public void setProbabilidad(double probabilidad) { this.probabilidad = probabilidad; }

        public String getConfianza() { return confianza; }
        public void setConfianza(String confianza) { this.confianza = confianza; }

        public String getSeveridad() { return severidad; }
        public void setSeveridad(String severidad) { this.severidad = severidad; }

        public String getModelVersion() { return modelVersion; }
        public void setModelVersion(String modelVersion) { this.modelVersion = modelVersion; }

        public boolean isConcluyente() { return concluyente; }
        public void setConcluyente(boolean concluyente) { this.concluyente = concluyente; }

        public List<RecomendacionItem> getRecomendaciones() { return recomendaciones; }
        public void setRecomendaciones(List<RecomendacionItem> recomendaciones) { this.recomendaciones = recomendaciones; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }

    public static class MlPredictionItem {
        private String classCode;
        private String label;
        private double probability;

        public MlPredictionItem() {}

        public String getClassCode() { return classCode; }
        public void setClassCode(String classCode) { this.classCode = classCode; }

        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }

        public double getProbability() { return probability; }
        public void setProbability(double probability) { this.probability = probability; }
    }

    public static class MlPredictionResponse {
        private String requestId;
        private String modelVersion;
        private String plantOrgan;
        private String status;               // "ok" | "not_conclusive"
        private MlPredictionItem topPrediction;
        private List<MlPredictionItem> predictions;
        private Map<String, Object> quality;

        public MlPredictionResponse() {}

        public String getRequestId() { return requestId; }
        public void setRequestId(String requestId) { this.requestId = requestId; }

        public String getModelVersion() { return modelVersion; }
        public void setModelVersion(String modelVersion) { this.modelVersion = modelVersion; }

        public String getPlantOrgan() { return plantOrgan; }
        public void setPlantOrgan(String plantOrgan) { this.plantOrgan = plantOrgan; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public MlPredictionItem getTopPrediction() { return topPrediction; }
        public void setTopPrediction(MlPredictionItem topPrediction) { this.topPrediction = topPrediction; }

        public List<MlPredictionItem> getPredictions() { return predictions; }
        public void setPredictions(List<MlPredictionItem> predictions) { this.predictions = predictions; }

        public Map<String, Object> getQuality() { return quality; }
        public void setQuality(Map<String, Object> quality) { this.quality = quality; }
    }
}
