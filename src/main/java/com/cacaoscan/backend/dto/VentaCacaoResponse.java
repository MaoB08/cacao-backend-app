package com.cacaoscan.backend.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class VentaCacaoResponse {

    private UUID id;
    private BigDecimal cantidad;
    private String unidad;
    private String tipoGrano;
    private String humedad;
    private BigDecimal totalEstimado;
    private String fecha;

    public VentaCacaoResponse() {}

    public VentaCacaoResponse(UUID id, BigDecimal cantidad, String unidad, String tipoGrano, String humedad, BigDecimal totalEstimado, String fecha) {
        this.id = id;
        this.cantidad = cantidad;
        this.unidad = unidad;
        this.tipoGrano = tipoGrano;
        this.humedad = humedad;
        this.totalEstimado = totalEstimado;
        this.fecha = fecha;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public BigDecimal getCantidad() {
        return cantidad;
    }

    public void setCantidad(BigDecimal cantidad) {
        this.cantidad = cantidad;
    }

    public String getUnidad() {
        return unidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }

    public String getTipoGrano() {
        return tipoGrano;
    }

    public void setTipoGrano(String tipoGrano) {
        this.tipoGrano = tipoGrano;
    }

    public String getHumedad() {
        return humedad;
    }

    public void setHumedad(String humedad) {
        this.humedad = humedad;
    }

    public BigDecimal getTotalEstimado() {
        return totalEstimado;
    }

    public void setTotalEstimado(BigDecimal totalEstimado) {
        this.totalEstimado = totalEstimado;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }
}
