package com.cacaoscan.backend.controller;

import com.cacaoscan.backend.dto.DeteccionGeoreferenciadaDTOs.*;
import com.cacaoscan.backend.service.DeteccionGeoreferenciadaService;
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
@Tag(name = "Detecciones Georreferenciadas", description = "Registro y consulta de enfermedades detectadas por IA con ubicación GPS exacta")
public class DeteccionGeoreferenciadaController {

    private final DeteccionGeoreferenciadaService deteccionService;

    public DeteccionGeoreferenciadaController(DeteccionGeoreferenciadaService deteccionService) {
        this.deteccionService = deteccionService;
    }

    @PostMapping("/detecciones")
    @Operation(summary = "Registrar detección",
            description = "Registra una enfermedad detectada por la IA con su foto, confianza y coordenadas GPS. " +
                    "El lote se asigna automáticamente mediante ST_Contains si la coordenada cae dentro de un lote registrado.")
    public ResponseEntity<DeteccionResponse> crearDeteccion(
            @Valid @RequestBody CrearDeteccionRequest request,
            Principal principal) {
        validarPrincipal(principal);
        DeteccionResponse response = deteccionService.crearDeteccion(request, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/lotes/{loteId}/detecciones")
    @Operation(summary = "Detecciones por lote",
            description = "Obtiene todos los marcadores de enfermedades detectadas dentro del lote para renderizar en el mapa")
    public ResponseEntity<List<DeteccionResponse>> listarPorLote(
            @PathVariable UUID loteId,
            Principal principal) {
        validarPrincipal(principal);
        List<DeteccionResponse> detecciones = deteccionService.listarPorLote(loteId, principal.getName());
        return ResponseEntity.ok(detecciones);
    }

    @GetMapping("/detecciones/{id}")
    @Operation(summary = "Detalle de detección",
            description = "Obtiene la información completa de una detección incluyendo foto, enfermedad, confianza y ubicación")
    public ResponseEntity<DeteccionResponse> obtenerDetalle(
            @PathVariable UUID id,
            Principal principal) {
        validarPrincipal(principal);
        DeteccionResponse response = deteccionService.obtenerDetalle(id, principal.getName());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/detecciones/mapa")
    @Operation(summary = "Todas las detecciones para mapa",
            description = "Obtiene todos los marcadores de detecciones del usuario para la vista global del mapa")
    public ResponseEntity<List<DeteccionMarkerResponse>> obtenerTodasMapa(Principal principal) {
        validarPrincipal(principal);
        List<DeteccionMarkerResponse> markers = deteccionService.obtenerTodasMapa(principal.getName());
        return ResponseEntity.ok(markers);
    }

    private void validarPrincipal(Principal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autorizado");
        }
    }
}
