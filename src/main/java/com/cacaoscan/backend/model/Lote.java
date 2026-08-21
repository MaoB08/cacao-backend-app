package com.cacaoscan.backend.model;

import jakarta.persistence.*;
import org.locationtech.jts.geom.Polygon;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entidad que representa un lote/parcela agrícola dentro de una finca.
 * La geometría del polígono se almacena como GEOMETRY(Polygon, 4326) en PostGIS.
 * El área y perímetro se calculan en el backend mediante funciones geodésicas de PostGIS.
 */
@Entity
@Table(name = "lotes")
public class Lote {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "finca_id", nullable = false)
    private Finca finca;

    @Column(nullable = false, length = 200)
    private String nombre;

    @Column(name = "variedad_cacao", length = 100)
    private String variedadCacao;

    @Column(name = "ano_siembra")
    private Integer anoSiembra;

    @Column(name = "numero_plantas")
    private Integer numeroPlantas;

    /**
     * Polígono geográfico del lote en coordenadas WGS 84 (SRID 4326).
     * Se genera cerrando la polyline registrada por el GPS del agricultor.
     */
    @Column(nullable = false, columnDefinition = "GEOMETRY(Polygon, 4326)")
    private Polygon geometria;

    @Column(name = "area_hectareas", precision = 10, scale = 4)
    private BigDecimal areaHectareas;

    @Column(name = "perimetro_metros", precision = 12, scale = 2)
    private BigDecimal perimetroMetros;

    @Column(name = "color_hex", nullable = false, length = 9)
    private String colorHex = "#4CAF50";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoLote estado = EstadoLote.ACTIVO;

    @Column(columnDefinition = "TEXT")
    private String notas;

    @OneToMany(mappedBy = "lote", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Inspeccion> inspecciones = new ArrayList<>();

    @OneToMany(mappedBy = "lote", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DeteccionGeoreferenciada> detecciones = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ======================== Getters & Setters ========================

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Finca getFinca() {
        return finca;
    }

    public void setFinca(Finca finca) {
        this.finca = finca;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getVariedadCacao() {
        return variedadCacao;
    }

    public void setVariedadCacao(String variedadCacao) {
        this.variedadCacao = variedadCacao;
    }

    public Integer getAnoSiembra() {
        return anoSiembra;
    }

    public void setAnoSiembra(Integer anoSiembra) {
        this.anoSiembra = anoSiembra;
    }

    public Integer getNumeroPlantas() {
        return numeroPlantas;
    }

    public void setNumeroPlantas(Integer numeroPlantas) {
        this.numeroPlantas = numeroPlantas;
    }

    public Polygon getGeometria() {
        return geometria;
    }

    public void setGeometria(Polygon geometria) {
        this.geometria = geometria;
    }

    public BigDecimal getAreaHectareas() {
        return areaHectareas;
    }

    public void setAreaHectareas(BigDecimal areaHectareas) {
        this.areaHectareas = areaHectareas;
    }

    public BigDecimal getPerimetroMetros() {
        return perimetroMetros;
    }

    public void setPerimetroMetros(BigDecimal perimetroMetros) {
        this.perimetroMetros = perimetroMetros;
    }

    public String getColorHex() {
        return colorHex;
    }

    public void setColorHex(String colorHex) {
        this.colorHex = colorHex;
    }

    public EstadoLote getEstado() {
        return estado;
    }

    public void setEstado(EstadoLote estado) {
        this.estado = estado;
    }

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }

    public List<Inspeccion> getInspecciones() {
        return inspecciones;
    }

    public void setInspecciones(List<Inspeccion> inspecciones) {
        this.inspecciones = inspecciones;
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
