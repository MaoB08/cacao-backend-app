package com.cacaoscan.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entidad que representa una inspección de campo realizada sobre un lote.
 * Cada inspección registra cuántas plantas se revisaron,
 * cuántas estaban enfermas y las detecciones asociadas.
 */
@Entity
@Table(name = "inspecciones")
public class Inspeccion {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_id", nullable = false)
    private Lote lote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "fecha_inspeccion", nullable = false)
    private LocalDateTime fechaInspeccion;

    @Column(name = "plantas_inspeccionadas", nullable = false)
    private int plantasInspeccionadas = 0;

    @Column(name = "plantas_enfermas", nullable = false)
    private int plantasEnfermas = 0;

    @Column(name = "total_enfermedades", nullable = false)
    private int totalEnfermedades = 0;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoInspeccion estado = EstadoInspeccion.COMPLETADA;

    @Column(name = "duracion_minutos")
    private Integer duracionMinutos;

    @OneToMany(mappedBy = "inspeccion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DeteccionGeoreferenciada> detecciones = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (fechaInspeccion == null) {
            fechaInspeccion = LocalDateTime.now();
        }
    }

    // ======================== Getters & Setters ========================

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Lote getLote() {
        return lote;
    }

    public void setLote(Lote lote) {
        this.lote = lote;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public LocalDateTime getFechaInspeccion() {
        return fechaInspeccion;
    }

    public void setFechaInspeccion(LocalDateTime fechaInspeccion) {
        this.fechaInspeccion = fechaInspeccion;
    }

    public int getPlantasInspeccionadas() {
        return plantasInspeccionadas;
    }

    public void setPlantasInspeccionadas(int plantasInspeccionadas) {
        this.plantasInspeccionadas = plantasInspeccionadas;
    }

    public int getPlantasEnfermas() {
        return plantasEnfermas;
    }

    public void setPlantasEnfermas(int plantasEnfermas) {
        this.plantasEnfermas = plantasEnfermas;
    }

    public int getTotalEnfermedades() {
        return totalEnfermedades;
    }

    public void setTotalEnfermedades(int totalEnfermedades) {
        this.totalEnfermedades = totalEnfermedades;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public EstadoInspeccion getEstado() {
        return estado;
    }

    public void setEstado(EstadoInspeccion estado) {
        this.estado = estado;
    }

    public Integer getDuracionMinutos() {
        return duracionMinutos;
    }

    public void setDuracionMinutos(Integer duracionMinutos) {
        this.duracionMinutos = duracionMinutos;
    }

    public List<DeteccionGeoreferenciada> getDetecciones() {
        return detecciones;
    }

    public void setDetecciones(List<DeteccionGeoreferenciada> detecciones) {
        this.detecciones = detecciones;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
