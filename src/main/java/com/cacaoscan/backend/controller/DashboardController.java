package com.cacaoscan.backend.controller;

import com.cacaoscan.backend.dto.AgricultorDashboardResponse;
import com.cacaoscan.backend.dto.ExpertoDashboardResponse;
import com.cacaoscan.backend.model.Rol;
import com.cacaoscan.backend.model.Usuario;
import com.cacaoscan.backend.repository.UsuarioRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.time.LocalDate;
import java.util.Arrays;

/**
 * Endpoints para obtener los datos resumidos del Agricultor y el Experto.
 * NOTA: Los datos devueltos actualmente son simulaciones/mock realistas para desarrollo local
 * y deben conectarse a entidades de base de datos reales para producción.
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Dashboard", description = "Endpoints para obtener los datos resumidos del Agricultor y el Experto")
public class DashboardController {

    private final UsuarioRepository usuarioRepository;

    public DashboardController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/agricultores/dashboard")
    @Operation(summary = "Obtener Dashboard Agricultor", description = "Devuelve el resumen de diagnósticos, alertas y lista reciente del agricultor autenticado")
    public ResponseEntity<AgricultorDashboardResponse> getAgricultorDashboard(Principal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autorizado");
        }
        
        Usuario usuario = usuarioRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        if (usuario.getRol() != Rol.AGRICULTOR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Rol no permitido");
        }

        // Crear datos de ejemplo realistas
        var agricultorInfo = new AgricultorDashboardResponse.AgricultorInfo(
                usuario.getNombre(),
                usuario.getNombreFinca() != null ? usuario.getNombreFinca() : "Finca Piloto"
        );

        var resumen = new AgricultorDashboardResponse.ResumenInfo(
                12, // totalDiagnosticos
                2,  // alertasActivas
                LocalDate.now().minusDays(1) // ultimoAnalisis
        );

        var diagnosticos = Arrays.asList(
                new AgricultorDashboardResponse.DiagnosticoInfo(
                        "diag-001",
                        "https://images.unsplash.com/photo-1587132137056-bfbf0166836e?auto=format&fit=crop&q=80&w=200", // Monilia
                        "Moniliasis del Cacao",
                        0.94,
                        "ALTA",
                        LocalDate.now().minusDays(1)
                ),
                new AgricultorDashboardResponse.DiagnosticoInfo(
                        "diag-002",
                        "https://images.unsplash.com/photo-1599599810769-bcde5a160d32?auto=format&fit=crop&q=80&w=200", // Escoba de bruja
                        "Escoba de Bruja",
                        0.87,
                        "MEDIA",
                        LocalDate.now().minusDays(4)
                ),
                new AgricultorDashboardResponse.DiagnosticoInfo(
                        "diag-003",
                        "https://images.unsplash.com/photo-1528183429752-a97d0bf99b5a?auto=format&fit=crop&q=80&w=200", // Fitóftora
                        "Mazorca Negra (Phytophthora)",
                        0.91,
                        "ALTA",
                        LocalDate.now().minusDays(8)
                ),
                new AgricultorDashboardResponse.DiagnosticoInfo(
                        "diag-004",
                        "https://images.unsplash.com/photo-1601244000763-95770ca1a495?auto=format&fit=crop&q=80&w=200", // Sano
                        "Hojas y Fruto Saludables",
                        0.99,
                        "SALUDABLE",
                        LocalDate.now().minusDays(12)
                )
        );

        var response = new AgricultorDashboardResponse(agricultorInfo, resumen, diagnosticos);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/expertos/dashboard")
    @Operation(summary = "Obtener Dashboard Experto", description = "Devuelve los KPIs y lista de diagnósticos pendientes de validación para el experto autenticado")
    public ResponseEntity<ExpertoDashboardResponse> getExpertoDashboard(Principal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autorizado");
        }

        Usuario usuario = usuarioRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        if (usuario.getRol() != Rol.EXPERTO) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Rol no permitido");
        }

        var expertoInfo = new ExpertoDashboardResponse.ExpertoInfo(usuario.getNombre());

        var kpis = new ExpertoDashboardResponse.KpiInfo(
                8,  // agricultoresAsignados
                5,  // diagnosticosPendientes
                3   // alertasActivas
        );

        var pendientes = Arrays.asList(
                new ExpertoDashboardResponse.DiagnosticoPendienteInfo(
                        "diag-101",
                        "Juan Pérez",
                        "Finca La Esmeralda",
                        "Moniliasis del Cacao",
                        "ALTA",
                        LocalDate.now().minusDays(1)
                ),
                new ExpertoDashboardResponse.DiagnosticoPendienteInfo(
                        "diag-102",
                        "Maria Rodríguez",
                        "Finca El Recuerdo",
                        "Escoba de Bruja",
                        "MEDIA",
                        LocalDate.now().minusDays(2)
                ),
                new ExpertoDashboardResponse.DiagnosticoPendienteInfo(
                        "diag-103",
                        "José Delgado",
                        "Finca Villa Sandra",
                        "Mazorca Negra (Phytophthora)",
                        "ALTA",
                        LocalDate.now().minusDays(3)
                ),
                new ExpertoDashboardResponse.DiagnosticoPendienteInfo(
                        "diag-104",
                        "Pedro Nel",
                        "Finca Los Cacaos",
                        "Moniliasis del Cacao",
                        "MEDIA",
                        LocalDate.now().minusDays(5)
                )
        );

        var response = new ExpertoDashboardResponse(expertoInfo, kpis, pendientes);
        return ResponseEntity.ok(response);
    }
}
