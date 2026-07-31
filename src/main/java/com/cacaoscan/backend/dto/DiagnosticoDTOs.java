package com.cacaoscan.backend.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class DiagnosticoDTOs {

    public static class AnalisisRequest {
        private String imagenUri; // Base64 o URL
        private String simulacionTipo; // Opcional: "ENFERMA", "SANA", "MONILIASIS", "ESCOBA" para testing en campo

        public String getImagenUri() {
            return imagenUri;
        }

        public void setImagenUri(String imagenUri) {
            this.imagenUri = imagenUri;
        }

        public String getSimulacionTipo() {
            return simulacionTipo;
        }

        public void setSimulacionTipo(String simulacionTipo) {
            this.simulacionTipo = simulacionTipo;
        }
    }

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
        private String confianza;
        private String severidad;
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

        public String getConfianza() { return confianza; }
        public void setConfianza(String confianza) { this.confianza = confianza; }

        public String getSeveridad() { return severidad; }
        public void setSeveridad(String severidad) { this.severidad = severidad; }

        public List<RecomendacionItem> getRecomendaciones() { return recomendaciones; }
        public void setRecomendaciones(List<RecomendacionItem> recomendaciones) { this.recomendaciones = recomendaciones; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }
}
