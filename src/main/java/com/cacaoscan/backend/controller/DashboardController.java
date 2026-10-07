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
import java.util.List;

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
    private final com.cacaoscan.backend.repository.DiagnosticoRepository diagnosticoRepository;
    private final com.cacaoscan.backend.repository.ChatRepository chatRepository;

    public DashboardController(UsuarioRepository usuarioRepository,
                               com.cacaoscan.backend.repository.DiagnosticoRepository diagnosticoRepository,
                               com.cacaoscan.backend.repository.ChatRepository chatRepository) {
        this.usuarioRepository = usuarioRepository;
        this.diagnosticoRepository = diagnosticoRepository;
        this.chatRepository = chatRepository;
    }

    @GetMapping("/agricultores/dashboard")
    @Operation(summary = "Obtener Dashboard Agricultor", description = "Devuelve el resumen de diagnósticos, alertas y lista reciente del agricultor autenticado")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<AgricultorDashboardResponse> getAgricultorDashboard(Principal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autorizado");
        }
        
        Usuario usuario = usuarioRepository.findByIdentificador(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        if (usuario.getRol() != Rol.AGRICULTOR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Rol no permitido");
        }

        // Obtener información de la finca
        String nombreFinca = usuario.getNombreFinca();
        if (nombreFinca == null || nombreFinca.isBlank()) {
            nombreFinca = "Finca de " + usuario.getNombre();
        }

        var agricultorInfo = new AgricultorDashboardResponse.AgricultorInfo(
                usuario.getNombre(),
                nombreFinca
        );

        // Obtener resumen de diagnósticos reales
        int totalDiag = (int) diagnosticoRepository.countByUsuarioId(usuario.getId());
        int alertasActivas = (int) diagnosticoRepository.countAlertasActivasByUsuarioId(usuario.getId());
        
        java.util.Optional<com.cacaoscan.backend.model.Diagnostico> ultimoDiagOpt = 
                diagnosticoRepository.findFirstByUsuarioOrderByCreatedAtDesc(usuario);
        
        LocalDate ultimoAnalisis = ultimoDiagOpt.map(d -> d.getCreatedAt().toLocalDate()).orElse(null);

        var resumen = new AgricultorDashboardResponse.ResumenInfo(
                totalDiag,
                alertasActivas,
                ultimoAnalisis
        );

        // Obtener diagnósticos recientes reales
        List<com.cacaoscan.backend.model.Diagnostico> diagnosticosList = 
                diagnosticoRepository.findTop5ByUsuarioOrderByCreatedAtDesc(usuario);

        List<AgricultorDashboardResponse.DiagnosticoInfo> diagnosticos = diagnosticosList.stream()
                .map(d -> new AgricultorDashboardResponse.DiagnosticoInfo(
                        d.getId().toString(),
                        d.getImagenUrl(),
                        d.getNombreEnfermedad(),
                        parseConfianza(d.getConfianza()),
                        d.getSeveridad(),
                        d.getCreatedAt().toLocalDate()
                ))
                .toList();

        var response = new AgricultorDashboardResponse(agricultorInfo, resumen, diagnosticos);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/expertos/dashboard")
    @Operation(summary = "Obtener Dashboard Experto", description = "Devuelve los KPIs y lista de diagnósticos pendientes de validación para el experto autenticado")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<ExpertoDashboardResponse> getExpertoDashboard(Principal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autorizado");
        }

        Usuario usuario = usuarioRepository.findByIdentificador(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        if (usuario.getRol() != Rol.EXPERTO) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Rol no permitido");
        }

        var expertoInfo = new ExpertoDashboardResponse.ExpertoInfo(usuario.getNombre());

        // Consultar agricultores asignados reales (con chats activos)
        long agricultoresAsignados = chatRepository.countUniqueAgricultoresByExpertoNombre(usuario.getNombre());
        if (agricultoresAsignados == 0) {
            // Fallback si no tiene chats asignados
            agricultoresAsignados = chatRepository.countTotalUniqueAgricultores();
        }

        // Consultar diagnósticos totales con enfermedad y alertas activas
        long diagnosticosPendientes = diagnosticoRepository.countByTieneEnfermedadTrue();
        long alertasActivas = diagnosticoRepository.countAlertasTotales();

        var kpis = new ExpertoDashboardResponse.KpiInfo(
                (int) agricultoresAsignados,
                (int) diagnosticosPendientes,
                (int) alertasActivas
        );

        // Consultar diagnósticos reales pendientes de revisión
        List<com.cacaoscan.backend.model.Diagnostico> pendientesList = 
                diagnosticoRepository.findTop10ByTieneEnfermedadTrueOrderByCreatedAtDesc();

        List<ExpertoDashboardResponse.DiagnosticoPendienteInfo> pendientes = pendientesList.stream()
                .map(d -> {
                    String fincaName = d.getUsuario().getNombreFinca();
                    if (fincaName == null || fincaName.isBlank()) {
                        fincaName = "Finca de " + d.getUsuario().getNombre();
                    }
                    return new ExpertoDashboardResponse.DiagnosticoPendienteInfo(
                            d.getId().toString(),
                            d.getUsuario().getNombre(),
                            fincaName,
                            d.getNombreEnfermedad(),
                            d.getSeveridad(),
                            d.getCreatedAt().toLocalDate()
                    );
                })
                .toList();

        var response = new ExpertoDashboardResponse(expertoInfo, kpis, pendientes);
        return ResponseEntity.ok(response);
    }

    private double parseConfianza(String confianza) {
        if (confianza == null || confianza.isBlank()) {
            return 0.0;
        }
        try {
            String clean = confianza.replace("%", "").trim();
            double val = Double.parseDouble(clean);
            if (val > 1.0) {
                return val / 100.0;
            }
            return val;
        } catch (Exception e) {
            return 0.0;
        }
    }
}
