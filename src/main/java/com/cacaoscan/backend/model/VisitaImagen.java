package com.cacaoscan.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Imagen vinculada a una visita técnica, con referencia opcional
 * al lote inspeccionado y al diagnóstico/análisis IA.
 */
@Entity
@Table(name = "visita_imagenes")
public class VisitaImagen {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "visita_id", nullable = false)
    private VisitaTecnica visita;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_id")
    private Lote lote;

    @Column(name = "imagen_url", nullable = false, length = 500)
    private String imagenUrl;

    @Column(columnDefinition = "TEXT")
    private String notas;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analisis_id")
    private Diagnostico analisis;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // ======================== Getters & Setters ========================

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public VisitaTecnica getVisita() { return visita; }
    public void setVisita(VisitaTecnica visita) { this.visita = visita; }

    public Lote getLote() { return lote; }
    public void setLote(Lote lote) { this.lote = lote; }

    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }

    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }

    public Diagnostico getAnalisis() { return analisis; }
    public void setAnalisis(Diagnostico analisis) { this.analisis = analisis; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
