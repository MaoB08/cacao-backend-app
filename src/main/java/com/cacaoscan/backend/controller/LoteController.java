package com.cacaoscan.backend.controller;

import com.cacaoscan.backend.dto.LoteDTOs.*;
import com.cacaoscan.backend.service.LoteService;
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
@RequestMapping("/api/v1/lotes")
@Tag(name = "Lotes Agrícolas", description = "Gestión de lotes/parcelas con delimitación GPS y visualización en mapa")
public class LoteController {

    private final LoteService loteService;

    public LoteController(LoteService loteService) {
        this.loteService = loteService;
    }

    @PostMapping
    @Operation(summary = "Crear lote",
            description = "Registra un nuevo lote agrícola a partir de las coordenadas GPS capturadas durante el recorrido del agricultor. " +
                    "Construye el polígono, calcula área y perímetro geodésicos mediante PostGIS.")
    public ResponseEntity<LoteDetalleResponse> crearLote(
            @Valid @RequestBody CrearLoteRequest request,
            Principal principal) {
        validarPrincipal(principal);
        LoteDetalleResponse response = loteService.crearLote(request, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/mapa")
    @Operation(summary = "Lotes para mapa",
            description = "Obtiene todos los lotes del usuario con polígonos y marcadores de detecciones para renderizar en el mapa")
    public ResponseEntity<List<LoteMapaResponse>> obtenerLotesMapa(Principal principal) {
        validarPrincipal(principal);
        List<LoteMapaResponse> lotes = loteService.obtenerLotesMapa(principal.getName());
        return ResponseEntity.ok(lotes);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalle de lote",
            description = "Obtiene el detalle completo del lote con estadísticas de inspecciones y resumen de enfermedades")
    public ResponseEntity<LoteDetalleResponse> obtenerDetalle(
            @PathVariable UUID id,
            Principal principal) {
        validarPrincipal(principal);
        LoteDetalleResponse response = loteService.obtenerDetalle(id, principal.getName());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar lote", description = "Actualiza nombre, variedad, color y otros campos del lote")
    public ResponseEntity<LoteDetalleResponse> actualizarLote(
            @PathVariable UUID id,
            @Valid @RequestBody ActualizarLoteRequest request,
            Principal principal) {
        validarPrincipal(principal);
        LoteDetalleResponse response = loteService.actualizarLote(id, request, principal.getName());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar lote", description = "Elimina un lote y todas sus inspecciones y detecciones asociadas")
    public ResponseEntity<Void> eliminarLote(
            @PathVariable UUID id,
            Principal principal) {
        validarPrincipal(principal);
        loteService.eliminarLote(id, principal.getName());
        return ResponseEntity.noContent().build();
    }

    private void validarPrincipal(Principal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autorizado");
        }
    }
}
