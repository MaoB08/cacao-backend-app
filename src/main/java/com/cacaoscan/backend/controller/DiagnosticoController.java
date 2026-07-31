package com.cacaoscan.backend.controller;

import com.cacaoscan.backend.dto.DiagnosticoDTOs.*;
import com.cacaoscan.backend.service.DiagnosticoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/diagnosticos")
@Tag(name = "Diagnósticos IA", description = "Endpoints para análisis por Red Neuronal y gestión de historial de diagnósticos de cacao")
public class DiagnosticoController {

    private final DiagnosticoService diagnosticoService;

    public DiagnosticoController(DiagnosticoService diagnosticoService) {
        this.diagnosticoService = diagnosticoService;
    }

    @PostMapping("/analizar")
    @Operation(summary = "Analizar Imagen con Red Neuronal", description = "Procesa la imagen de la mazorca y devuelve la clasificación de enfermedad, confianza, severidad y recomendaciones")
    public ResponseEntity<DiagnosticoResponse> analizarImagen(@RequestBody AnalisisRequest request, Principal principal) {
        String userEmail = principal != null ? principal.getName() : "demo@cacaocare.com";
        DiagnosticoResponse response = diagnosticoService.procesarAnalisisIA(request, userEmail);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @Operation(summary = "Guardar Diagnóstico en Historial", description = "Persiste el resultado del diagnóstico en la base de datos de la finca del usuario")
    public ResponseEntity<DiagnosticoResponse> guardarDiagnostico(@RequestBody DiagnosticoResponse dto, Principal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autorizado");
        }
        DiagnosticoResponse saved = diagnosticoService.guardarDiagnostico(dto, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    @Operation(summary = "Obtener Historial de Diagnósticos", description = "Devuelve todos los diagnósticos analizados por el usuario ordenados por fecha descendente")
    public ResponseEntity<List<DiagnosticoResponse>> obtenerHistorial(Principal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autorizado");
        }
        List<DiagnosticoResponse> historial = diagnosticoService.obtenerHistorialUsuario(principal.getName());
        return ResponseEntity.ok(historial);
    }
}
