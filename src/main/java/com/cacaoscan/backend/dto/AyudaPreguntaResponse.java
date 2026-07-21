package com.cacaoscan.backend.dto;

import java.util.UUID;

public class AyudaPreguntaResponse {
    private UUID id;
    private String pregunta;
    private String respuesta;
    private String categoria;
    private int orden;

    public AyudaPreguntaResponse() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getPregunta() { return pregunta; }
    public void setPregunta(String pregunta) { this.pregunta = pregunta; }

    public String getRespuesta() { return respuesta; }
    public void setRespuesta(String respuesta) { this.respuesta = respuesta; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public int getOrden() { return orden; }
    public void setOrden(int orden) { this.orden = orden; }
}
