package com.cacaoscan.backend.controller;

import com.cacaoscan.backend.dto.VentaCacaoRequest;
import com.cacaoscan.backend.dto.VentaCacaoResponse;
import com.cacaoscan.backend.service.VentaCacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/v1/ventas")
@Tag(name = "Ventas de Cacao", description = "Endpoints para almacenar y consultar historial de ventas de cacao del agricultor")
public class VentaCacaoController {

    private static final Logger logger = LoggerFactory.getLogger(VentaCacaoController.class);

    private final VentaCacaoService ventaCacaoService;

    public VentaCacaoController(VentaCacaoService ventaCacaoService) {
        this.ventaCacaoService = ventaCacaoService;
    }

    @PostMapping
    @Operation(summary = "Registrar Venta", description = "Guarda una nueva estimación o registro de venta de cacao para el usuario autenticado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Venta registrada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de venta inválidos"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    public ResponseEntity<VentaCacaoResponse> registrarVenta(Principal principal,
                                                              @Valid @RequestBody VentaCacaoRequest request) {
        logger.info("📥 POST /ventas - Usuario: {}", principal != null ? principal.getName() : "NULL");
        logger.info("📦 Request body - cantidad: {}, unidad: {}, tipoGrano: {}, humedad: {}, totalEstimado: {}",
                request.getCantidad(), request.getUnidad(), request.getTipoGrano(),
                request.getHumedad(), request.getTotalEstimado());
        try {
            VentaCacaoResponse response = ventaCacaoService.registrarVenta(principal.getName(), request);
            logger.info("✅ Venta registrada con éxito, id: {}", response.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            logger.error("❌ Error al registrar venta para usuario {}: {}", principal.getName(), e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping
    @Operation(summary = "Listar Ventas", description = "Obtiene el historial completo de ventas registradas del agricultor autenticado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Historial obtenido exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    public ResponseEntity<List<VentaCacaoResponse>> obtenerVentas(Principal principal) {
        List<VentaCacaoResponse> ventas = ventaCacaoService.obtenerVentasUsuario(principal.getName());
        return ResponseEntity.ok(ventas);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar Venta", description = "Elimina un registro de venta específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Venta eliminada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Venta no encontrada"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    public ResponseEntity<Void> eliminarVenta(Principal principal, @PathVariable UUID id) {
        ventaCacaoService.eliminarVenta(principal.getName(), id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    @Operation(summary = "Limpiar Historial de Ventas", description = "Elimina todas las ventas registradas del usuario autenticado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Historial limpiado exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    public ResponseEntity<Void> eliminarTodasVentas(Principal principal) {
        ventaCacaoService.eliminarTodasVentas(principal.getName());
        return ResponseEntity.noContent().build();
    }
}
