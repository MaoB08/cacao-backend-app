package com.cacaoscan.backend.dto;

import java.time.LocalDate;
import java.util.List;

public record ExpertoDashboardResponse(
    ExpertoInfo experto,
    KpiInfo kpis,
    List<DiagnosticoPendienteInfo> diagnosticosPendientesRevision
) {
    public record ExpertoInfo(String nombre) {}
    
    public record KpiInfo(
        int agricultoresAsignados,
        int diagnosticosPendientes,
        int alertasActivas
    ) {}
    
    public record DiagnosticoPendienteInfo(
        String id,
        String agricultor,
        String finca,
        String enfermedadDetectada,
        String severidad,
        LocalDate fecha
    ) {}
}
