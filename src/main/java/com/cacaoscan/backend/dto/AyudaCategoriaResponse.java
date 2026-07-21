package com.cacaoscan.backend.dto;

import java.util.UUID;

public class AyudaCategoriaResponse {
    private UUID id;
    private String nombre;
    private String icono;
    private String color;
    private String descripcion;
    private int orden;

    public AyudaCategoriaResponse() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getIcono() { return icono; }
    public void setIcono(String icono) { this.icono = icono; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public int getOrden() { return orden; }
    public void setOrden(int orden) { this.orden = orden; }
}
