package com.cacaoscan.backend.controller;

import com.cacaoscan.backend.dto.FincaDTOs.*;
import com.cacaoscan.backend.service.FincaService;
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
@RequestMapping("/api/v1/fincas")
@Tag(name = "Fincas", description = "Gestión de fincas agrícolas del productor de cacao")
public class FincaController {

    private final FincaService fincaService;

    public FincaController(FincaService fincaService) {
        this.fincaService = fincaService;
    }

    @PostMapping
    @Operation(summary = "Crear finca", description = "Registra una nueva finca agrícola para el usuario autenticado")
    public ResponseEntity<FincaResponse> crearFinca(
            @Valid @RequestBody CrearFincaRequest request,
            Principal principal) {
        validarPrincipal(principal);
        FincaResponse response = fincaService.crearFinca(request, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar fincas", description = "Obtiene todas las fincas activas del usuario autenticado")
    public ResponseEntity<List<FincaResponse>> listarFincas(Principal principal) {
        validarPrincipal(principal);
        List<FincaResponse> fincas = fincaService.listarFincas(principal.getName());
        return ResponseEntity.ok(fincas);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalle de finca", description = "Obtiene el detalle completo de una finca con sus lotes")
    public ResponseEntity<FincaDetalleResponse> obtenerDetalle(
            @PathVariable UUID id,
            Principal principal) {
        validarPrincipal(principal);
        FincaDetalleResponse response = fincaService.obtenerDetalle(id, principal.getName());
        return ResponseEntity.ok(response);
    }

    private void validarPrincipal(Principal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autorizado");
        }
    }
}
