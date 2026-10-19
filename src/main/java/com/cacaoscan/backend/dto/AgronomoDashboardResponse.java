package com.cacaoscan.backend.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO para el dashboard del agrónomo.
 */
public class AgronomoDashboardResponse {

    private AgronomoInfo agronomo;
    private ContadoresInfo contadores;
    private List<ProximaVisitaInfo> proximasVisitas;

    public AgronomoDashboardResponse() {}

    public AgronomoDashboardResponse(AgronomoInfo agronomo, ContadoresInfo contadores,
                                     List<ProximaVisitaInfo> proximasVisitas) {
        this.agronomo = agronomo;
        this.contadores = contadores;
        this.proximasVisitas = proximasVisitas;
    }

    public AgronomoInfo getAgronomo() { return agronomo; }
    public void setAgronomo(AgronomoInfo agronomo) { this.agronomo = agronomo; }

    public ContadoresInfo getContadores() { return contadores; }
    public void setContadores(ContadoresInfo contadores) { this.contadores = contadores; }

    public List<ProximaVisitaInfo> getProximasVisitas() { return proximasVisitas; }
    public void setProximasVisitas(List<ProximaVisitaInfo> proximasVisitas) { this.proximasVisitas = proximasVisitas; }

    // ======================== Inner classes ========================

    public static class AgronomoInfo {
        private String nombre;
        private long fincasAsignadas;

        public AgronomoInfo() {}

        public AgronomoInfo(String nombre, long fincasAsignadas) {
            this.nombre = nombre;
            this.fincasAsignadas = fincasAsignadas;
        }

        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }

        public long getFincasAsignadas() { return fincasAsignadas; }
        public void setFincasAsignadas(long fincasAsignadas) { this.fincasAsignadas = fincasAsignadas; }
    }

    public static class ContadoresInfo {
        private long programadas;
        private long enCurso;
        private long completadasMes;

        public ContadoresInfo() {}

        public ContadoresInfo(long programadas, long enCurso, long completadasMes) {
            this.programadas = programadas;
            this.enCurso = enCurso;
            this.completadasMes = completadasMes;
        }

        public long getProgramadas() { return programadas; }
        public void setProgramadas(long programadas) { this.programadas = programadas; }

        public long getEnCurso() { return enCurso; }
        public void setEnCurso(long enCurso) { this.enCurso = enCurso; }

        public long getCompletadasMes() { return completadasMes; }
        public void setCompletadasMes(long completadasMes) { this.completadasMes = completadasMes; }
    }

    public static class ProximaVisitaInfo {
        private UUID id;
        private String estado;
        private LocalDateTime fechaProgramada;
        private String fincaNombre;
        private String fincaMunicipio;
        private String agricultorNombre;
        private String loteNombre;
        private String motivo;

        public ProximaVisitaInfo() {}

        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }

        public String getEstado() { return estado; }
        public void setEstado(String estado) { this.estado = estado; }

        public LocalDateTime getFechaProgramada() { return fechaProgramada; }
        public void setFechaProgramada(LocalDateTime fechaProgramada) { this.fechaProgramada = fechaProgramada; }

        public String getFincaNombre() { return fincaNombre; }
        public void setFincaNombre(String fincaNombre) { this.fincaNombre = fincaNombre; }

        public String getFincaMunicipio() { return fincaMunicipio; }
        public void setFincaMunicipio(String fincaMunicipio) { this.fincaMunicipio = fincaMunicipio; }

        public String getAgricultorNombre() { return agricultorNombre; }
        public void setAgricultorNombre(String agricultorNombre) { this.agricultorNombre = agricultorNombre; }

        public String getLoteNombre() { return loteNombre; }
        public void setLoteNombre(String loteNombre) { this.loteNombre = loteNombre; }

        public String getMotivo() { return motivo; }
        public void setMotivo(String motivo) { this.motivo = motivo; }
    }
}
