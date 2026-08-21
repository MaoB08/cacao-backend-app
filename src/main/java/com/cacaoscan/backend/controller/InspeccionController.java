package com.cacaoscan.backend.controller;

import com.cacaoscan.backend.dto.InspeccionDTOs.*;
import com.cacaoscan.backend.service.InspeccionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Inspecciones", description = "Registro y consulta de inspecciones de campo realizadas sobre lotes agrícolas")
public class InspeccionController {

    private final InspeccionService inspeccionService;

    public InspeccionController(InspeccionService inspeccionService) {
        this.inspeccionService = inspeccionService;
    }

    @PostMapping("/lotes/{loteId}/inspecciones")
    @Operation(summary = "Crear inspección", description = "Registra una nueva inspección de campo para el lote especificado")
    public ResponseEntity<InspeccionResponse> crearInspeccion(
            @PathVariable UUID loteId,
            @Valid @RequestBody CrearInspeccionRequest request,
            Principal principal) {
        validarPrincipal(principal);
        request.setLoteId(loteId);
        InspeccionResponse response = inspeccionService.crearInspeccion(request, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/lotes/{loteId}/inspecciones")
    @Operation(summary = "Historial de inspecciones", description = "Obtiene todas las inspecciones realizadas en el lote ordenadas por fecha descendente")
    public ResponseEntity<List<InspeccionResponse>> listarPorLote(
            @PathVariable UUID loteId,
            Principal principal) {
        validarPrincipal(principal);
        List<InspeccionResponse> inspecciones = inspeccionService.listarPorLote(loteId, principal.getName());
        return ResponseEntity.ok(inspecciones);
    }

    @GetMapping("/inspecciones/{id}")
    @Operation(summary = "Detalle de inspección", description = "Obtiene el detalle completo de una inspección con sus detecciones de enfermedades")
    public ResponseEntity<InspeccionDetalleResponse> obtenerDetalle(
            @PathVariable UUID id,
            Principal principal) {
        validarPrincipal(principal);
        InspeccionDetalleResponse response = inspeccionService.obtenerDetalle(id, principal.getName());
        return ResponseEntity.ok(response);
    }

    private void validarPrincipal(Principal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autorizado");
        }
    }
}
