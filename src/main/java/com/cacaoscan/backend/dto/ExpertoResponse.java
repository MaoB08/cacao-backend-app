package com.cacaoscan.backend.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class ExpertoResponse {
    private UUID id;
    private String nombre;
    private String especialidad;
    private String descripcion;
    private BigDecimal rating;
    private String fotoPerfil;
    private boolean disponible;
    private Integer tiempoEspera;

    public ExpertoResponse() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEspecialidad() { return especialidad; }
    public void setEspecialidad(String especialidad) { this.especialidad = especialidad; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public BigDecimal getRating() { return rating; }
    public void setRating(BigDecimal rating) { this.rating = rating; }

    public String getFotoPerfil() { return fotoPerfil; }
    public void setFotoPerfil(String fotoPerfil) { this.fotoPerfil = fotoPerfil; }

    public boolean isDisponible() { return disponible; }
    public void setDisponible(boolean disponible) { this.disponible = disponible; }

    public Integer getTiempoEspera() { return tiempoEspera; }
    public void setTiempoEspera(Integer tiempoEspera) { this.tiempoEspera = tiempoEspera; }
}
