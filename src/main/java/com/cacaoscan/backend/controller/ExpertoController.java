package com.cacaoscan.backend.controller;

import com.cacaoscan.backend.dto.ExpertoResponse;
import com.cacaoscan.backend.service.ExpertoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/expertos")
@Tag(name = "Expertos", description = "Endpoints para listar y buscar expertos disponibles")
public class ExpertoController {

    private final ExpertoService expertoService;

    public ExpertoController(ExpertoService expertoService) {
        this.expertoService = expertoService;
    }

    @GetMapping
    @Operation(summary = "Listar Expertos", description = "Devuelve la lista de expertos activos con filtros opcionales de especialidad y búsqueda")
    public ResponseEntity<List<ExpertoResponse>> listarExpertos(
            @RequestParam(required = false) String especialidad,
            @RequestParam(required = false) String busqueda) {
        List<ExpertoResponse> expertos = expertoService.listarExpertos(especialidad, busqueda);
        return ResponseEntity.ok(expertos);
    }

    @GetMapping("/especialidades")
    @Operation(summary = "Listar Especialidades", description = "Devuelve las especialidades únicas de los expertos activos")
    public ResponseEntity<List<String>> listarEspecialidades() {
        List<String> especialidades = expertoService.listarEspecialidades();
        return ResponseEntity.ok(especialidades);
    }
}
