package com.cacaoscan.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entidad que representa una visita técnica del agrónomo a una finca.
 * Ciclo de vida: PROGRAMADA → EN_CURSO → COMPLETADA
 *                PROGRAMADA → CANCELADA
 *                PROGRAMADA → REPROGRAMADA (conserva el registro original)
 */
@Entity
@Table(name = "visitas_tecnicas")
public class VisitaTecnica {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agronomo_id", nullable = false)
    private Usuario agronomo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "finca_id", nullable = false)
    private Finca finca;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_id")
    private Lote lote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agricultor_id", nullable = false)
    private Usuario agricultor;

    // ======================== Agenda ========================

    @Column(name = "fecha_programada", nullable = false)
    private LocalDateTime fechaProgramada;

    @Column(name = "duracion_estimada_min")
    private Integer duracionEstimadaMin;

    @Column(length = 500)
    private String motivo;

    @Column(name = "notas_previas", columnDefinition = "TEXT")
    private String notasPrevias;

    // ======================== Ejecución ========================

    @Column(name = "fecha_inicio_real")
    private LocalDateTime fechaInicioReal;

    @Column(name = "fecha_fin_real")
    private LocalDateTime fechaFinReal;

    @Column(name = "duracion_real_min")
    private Integer duracionRealMin;

    // ======================== Resultados ========================

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Column(columnDefinition = "TEXT")
    private String recomendaciones;

    @Column(name = "plantas_revisadas")
    private Integer plantasRevisadas;

    @Column(name = "plantas_enfermas")
    private Integer plantasEnfermas;

    @Column(name = "informe_final", columnDefinition = "TEXT")
    private String informeFinal;

    // ======================== Estado ========================

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoVisita estado = EstadoVisita.PROGRAMADA;

    @Column(name = "motivo_cancelacion", columnDefinition = "TEXT")
    private String motivoCancelacion;

    // ======================== Imágenes ========================

    @OneToMany(mappedBy = "visita", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VisitaImagen> imagenes = new ArrayList<>();

    // ======================== Auditoría ========================

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

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Usuario getAgronomo() { return agronomo; }
    public void setAgronomo(Usuario agronomo) { this.agronomo = agronomo; }

    public Finca getFinca() { return finca; }
    public void setFinca(Finca finca) { this.finca = finca; }

    public Lote getLote() { return lote; }
    public void setLote(Lote lote) { this.lote = lote; }

    public Usuario getAgricultor() { return agricultor; }
    public void setAgricultor(Usuario agricultor) { this.agricultor = agricultor; }

    public LocalDateTime getFechaProgramada() { return fechaProgramada; }
    public void setFechaProgramada(LocalDateTime fechaProgramada) { this.fechaProgramada = fechaProgramada; }

    public Integer getDuracionEstimadaMin() { return duracionEstimadaMin; }
    public void setDuracionEstimadaMin(Integer duracionEstimadaMin) { this.duracionEstimadaMin = duracionEstimadaMin; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public String getNotasPrevias() { return notasPrevias; }
    public void setNotasPrevias(String notasPrevias) { this.notasPrevias = notasPrevias; }

    public LocalDateTime getFechaInicioReal() { return fechaInicioReal; }
    public void setFechaInicioReal(LocalDateTime fechaInicioReal) { this.fechaInicioReal = fechaInicioReal; }

    public LocalDateTime getFechaFinReal() { return fechaFinReal; }
    public void setFechaFinReal(LocalDateTime fechaFinReal) { this.fechaFinReal = fechaFinReal; }

    public Integer getDuracionRealMin() { return duracionRealMin; }
    public void setDuracionRealMin(Integer duracionRealMin) { this.duracionRealMin = duracionRealMin; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public String getRecomendaciones() { return recomendaciones; }
    public void setRecomendaciones(String recomendaciones) { this.recomendaciones = recomendaciones; }

    public Integer getPlantasRevisadas() { return plantasRevisadas; }
    public void setPlantasRevisadas(Integer plantasRevisadas) { this.plantasRevisadas = plantasRevisadas; }

    public Integer getPlantasEnfermas() { return plantasEnfermas; }
    public void setPlantasEnfermas(Integer plantasEnfermas) { this.plantasEnfermas = plantasEnfermas; }

    public String getInformeFinal() { return informeFinal; }
    public void setInformeFinal(String informeFinal) { this.informeFinal = informeFinal; }

    public EstadoVisita getEstado() { return estado; }
    public void setEstado(EstadoVisita estado) { this.estado = estado; }

    public String getMotivoCancelacion() { return motivoCancelacion; }
    public void setMotivoCancelacion(String motivoCancelacion) { this.motivoCancelacion = motivoCancelacion; }

    public List<VisitaImagen> getImagenes() { return imagenes; }
    public void setImagenes(List<VisitaImagen> imagenes) { this.imagenes = imagenes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
