package com.cacaoscan.backend.dto;

import jakarta.validation.Valid;
import java.math.BigDecimal;

public class PerfilUpdateRequest {
    private String nombre;
    private String telefono;
    private Boolean notificacionesActivas;

    @Valid
    private FincaUpdate finca;

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public Boolean getNotificacionesActivas() { return notificacionesActivas; }
    public void setNotificacionesActivas(Boolean notificacionesActivas) { this.notificacionesActivas = notificacionesActivas; }

    public FincaUpdate getFinca() { return finca; }
    public void setFinca(FincaUpdate finca) { this.finca = finca; }

    public static class FincaUpdate {
        private String nombre;
        private String departamento;
        private String municipio;
        private BigDecimal hectareas;

        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public String getDepartamento() { return departamento; }
        public void setDepartamento(String departamento) { this.departamento = departamento; }
        public String getMunicipio() { return municipio; }
        public void setMunicipio(String municipio) { this.municipio = municipio; }
        public BigDecimal getHectareas() { return hectareas; }
        public void setHectareas(BigDecimal hectareas) { this.hectareas = hectareas; }
    }
}
