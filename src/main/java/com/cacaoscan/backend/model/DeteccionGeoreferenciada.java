package com.cacaoscan.backend.model;

import jakarta.persistence.*;
import org.locationtech.jts.geom.Point;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad que representa una detección de enfermedad realizada por la IA
 * con su ubicación geográfica exacta (Point SRID 4326).
 * Se vincula al lote correspondiente mediante ST_Contains en el backend.
 */
@Entity
@Table(name = "detecciones_georeferenciadas")
public class DeteccionGeoreferenciada {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_id")
    private Lote lote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inspeccion_id")
    private Inspeccion inspeccion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false, length = 150)
    private String enfermedad;

    @Column(name = "porcentaje_confianza", nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentajeConfianza;

    @Column(length = 50)
    private String severidad;

    @Column(name = "imagen_url", columnDefinition = "TEXT")
    private String imagenUrl;

    /**
     * Ubicación geográfica exacta del punto donde se detectó la enfermedad.
     * Se usa para renderizar Markers en el mapa y para asociar automáticamente
     * la detección al lote correcto mediante ST_Contains.
     */
    @Column(columnDefinition = "GEOMETRY(Point, 4326)")
    private Point ubicacion;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "fecha_deteccion", nullable = false)
    private LocalDateTime fechaDeteccion;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (fechaDeteccion == null) {
            fechaDeteccion = LocalDateTime.now();
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

    public Inspeccion getInspeccion() {
        return inspeccion;
    }

    public void setInspeccion(Inspeccion inspeccion) {
        this.inspeccion = inspeccion;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getEnfermedad() {
        return enfermedad;
    }

    public void setEnfermedad(String enfermedad) {
        this.enfermedad = enfermedad;
    }

    public BigDecimal getPorcentajeConfianza() {
        return porcentajeConfianza;
    }

    public void setPorcentajeConfianza(BigDecimal porcentajeConfianza) {
        this.porcentajeConfianza = porcentajeConfianza;
    }

    public String getSeveridad() {
        return severidad;
    }

    public void setSeveridad(String severidad) {
        this.severidad = severidad;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    public Point getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(Point ubicacion) {
        this.ubicacion = ubicacion;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public LocalDateTime getFechaDeteccion() {
        return fechaDeteccion;
    }

    public void setFechaDeteccion(LocalDateTime fechaDeteccion) {
        this.fechaDeteccion = fechaDeteccion;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
