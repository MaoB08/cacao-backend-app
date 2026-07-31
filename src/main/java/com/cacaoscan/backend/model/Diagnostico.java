package com.cacaoscan.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "diagnosticos")
public class Diagnostico {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "imagen_url", nullable = false, columnDefinition = "TEXT")
    private String imagenUrl;

    @Column(name = "tiene_enfermedad", nullable = false)
    private boolean tieneEnfermedad;

    @Column(name = "nombre_enfermedad", nullable = false, length = 150)
    private String nombreEnfermedad;

    @Column(name = "nombre_cientifico", length = 150)
    private String nombreCientifico;

    @Column(nullable = false, length = 50)
    private String confianza;

    @Column(nullable = false, length = 50)
    private String severidad;

    @Column(name = "recomendaciones_json", columnDefinition = "TEXT")
    private String recomendacionesJson;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    public boolean isTieneEnfermedad() {
        return tieneEnfermedad;
    }

    public void setTieneEnfermedad(boolean tieneEnfermedad) {
        this.tieneEnfermedad = tieneEnfermedad;
    }

    public String getNombreEnfermedad() {
        return nombreEnfermedad;
    }

    public void setNombreEnfermedad(String nombreEnfermedad) {
        this.nombreEnfermedad = nombreEnfermedad;
    }

    public String getNombreCientifico() {
        return nombreCientifico;
    }

    public void setNombreCientifico(String nombreCientifico) {
        this.nombreCientifico = nombreCientifico;
    }

    public String getConfianza() {
        return confianza;
    }

    public void setConfianza(String confianza) {
        this.confianza = confianza;
    }

    public String getSeveridad() {
        return severidad;
    }

    public void setSeveridad(String severidad) {
        this.severidad = severidad;
    }

    public String getRecomendacionesJson() {
        return recomendacionesJson;
    }

    public void setRecomendacionesJson(String recomendacionesJson) {
        this.recomendacionesJson = recomendacionesJson;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
