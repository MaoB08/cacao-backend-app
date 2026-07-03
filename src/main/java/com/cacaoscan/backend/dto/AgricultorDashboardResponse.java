package com.cacaoscan.backend.dto;

import java.time.LocalDate;
import java.util.List;

public record AgricultorDashboardResponse(
    AgricultorInfo agricultor,
    ResumenInfo resumen,
    List<DiagnosticoInfo> diagnosticosRecientes
) {
    public record AgricultorInfo(String nombre, String nombreFinca) {}
    
    public record ResumenInfo(
        int totalDiagnosticos,
        int alertasActivas,
        LocalDate ultimoAnalisis
    ) {}
    
    public record DiagnosticoInfo(
        String id,
        String imagenUrl,
        String enfermedadDetectada,
        double nivelConfianza,
        String severidad, // "ALTA" | "MEDIA" | "BAJA" | "SALUDABLE"
        LocalDate fecha
    ) {}
}
