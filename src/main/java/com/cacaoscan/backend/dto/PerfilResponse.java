package com.cacaoscan.backend.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class PerfilResponse {
    private UUID id;
    private String nombre;
    private String email;
    private String telefono;
    private FincaInfo finca;
    private String fotoPerfil;
    private EstadisticasInfo estadisticas;
    private boolean notificacionesActivas;
    private String versionApp;

    public PerfilResponse() {}

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public FincaInfo getFinca() { return finca; }
    public void setFinca(FincaInfo finca) { this.finca = finca; }

    public String getFotoPerfil() { return fotoPerfil; }
    public void setFotoPerfil(String fotoPerfil) { this.fotoPerfil = fotoPerfil; }

    public EstadisticasInfo getEstadisticas() { return estadisticas; }
    public void setEstadisticas(EstadisticasInfo estadisticas) { this.estadisticas = estadisticas; }

    public boolean isNotificacionesActivas() { return notificacionesActivas; }
    public void setNotificacionesActivas(boolean notificacionesActivas) { this.notificacionesActivas = notificacionesActivas; }

    public String getVersionApp() { return versionApp; }
    public void setVersionApp(String versionApp) { this.versionApp = versionApp; }

    public static class FincaInfo {
        private String nombre;
        private String departamento;
        private String municipio;
        private BigDecimal hectareas;

        public FincaInfo() {}
        public FincaInfo(String nombre, String departamento, String municipio, BigDecimal hectareas) {
            this.nombre = nombre;
            this.departamento = departamento;
            this.municipio = municipio;
            this.hectareas = hectareas;
        }

        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public String getDepartamento() { return departamento; }
        public void setDepartamento(String departamento) { this.departamento = departamento; }
        public String getMunicipio() { return municipio; }
        public void setMunicipio(String municipio) { this.municipio = municipio; }
        public BigDecimal getHectareas() { return hectareas; }
        public void setHectareas(BigDecimal hectareas) { this.hectareas = hectareas; }
    }

    public static class EstadisticasInfo {
        private int totalAnalisis;
        private int estadoSaludPorcentaje;
        private String estadoSaludEtiqueta;

        public EstadisticasInfo() {}
        public EstadisticasInfo(int totalAnalisis, int estadoSaludPorcentaje, String estadoSaludEtiqueta) {
            this.totalAnalisis = totalAnalisis;
            this.estadoSaludPorcentaje = estadoSaludPorcentaje;
            this.estadoSaludEtiqueta = estadoSaludEtiqueta;
        }

        public int getTotalAnalisis() { return totalAnalisis; }
        public void setTotalAnalisis(int totalAnalisis) { this.totalAnalisis = totalAnalisis; }
        public int getEstadoSaludPorcentaje() { return estadoSaludPorcentaje; }
        public void setEstadoSaludPorcentaje(int estadoSaludPorcentaje) { this.estadoSaludPorcentaje = estadoSaludPorcentaje; }
        public String getEstadoSaludEtiqueta() { return estadoSaludEtiqueta; }
        public void setEstadoSaludEtiqueta(String estadoSaludEtiqueta) { this.estadoSaludEtiqueta = estadoSaludEtiqueta; }
    }
}
