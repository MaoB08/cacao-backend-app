package com.cacaoscan.backend.controller;

import com.cacaoscan.backend.dto.AgronomoDashboardResponse;
import com.cacaoscan.backend.dto.FincaDTOs.FincaResponse;
import com.cacaoscan.backend.service.AgronomoFincaService;
import com.cacaoscan.backend.service.VisitaTecnicaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/agronomo")
@Tag(name = "Agrónomo", description = "Endpoints específicos del agrónomo: fincas asignadas y dashboard")
public class AgronomoController {

    private final AgronomoFincaService agronomoFincaService;
    private final VisitaTecnicaService visitaTecnicaService;

    public AgronomoController(AgronomoFincaService agronomoFincaService,
                              VisitaTecnicaService visitaTecnicaService) {
        this.agronomoFincaService = agronomoFincaService;
        this.visitaTecnicaService = visitaTecnicaService;
    }

    @GetMapping("/fincas")
    @Operation(summary = "Listar fincas asignadas",
               description = "Devuelve las fincas activas asignadas al agrónomo autenticado, con búsqueda opcional")
    public ResponseEntity<Page<FincaResponse>> listarFincasAsignadas(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Principal principal) {
        validarPrincipal(principal);
        Page<FincaResponse> fincas = agronomoFincaService.listarFincasAsignadas(
                principal.getName(), search,
                PageRequest.of(page, size, Sort.by("finca.nombre").ascending()));
        return ResponseEntity.ok(fincas);
    }

    @GetMapping("/fincas/{id}/detalle")
    @Operation(summary = "Detalle de finca asignada",
               description = "Obtiene el detalle de una finca asignada al agrónomo, con lotes y datos del agricultor")
    public ResponseEntity<AgronomoFincaService.FincaDetalleAgronomoResponse> obtenerDetalleFinca(
            @PathVariable UUID id,
            Principal principal) {
        validarPrincipal(principal);
        return ResponseEntity.ok(agronomoFincaService.obtenerDetalleFinca(id, principal.getName()));
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Dashboard del agrónomo",
               description = "Resumen de agenda: próximas visitas, contadores del mes y fincas asignadas")
    public ResponseEntity<AgronomoDashboardResponse> getDashboard(Principal principal) {
        validarPrincipal(principal);
        return ResponseEntity.ok(visitaTecnicaService.obtenerDashboard(principal.getName()));
    }

    private void validarPrincipal(Principal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autorizado");
        }
    }
}
