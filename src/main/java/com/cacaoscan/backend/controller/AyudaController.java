package com.cacaoscan.backend.controller;

import com.cacaoscan.backend.dto.AyudaCategoriaResponse;
import com.cacaoscan.backend.dto.AyudaPreguntaResponse;
import com.cacaoscan.backend.service.AyudaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ayuda")
@Tag(name = "Ayuda", description = "Endpoints para el centro de ayuda (preguntas frecuentes y categorías)")
public class AyudaController {

    private final AyudaService ayudaService;

    public AyudaController(AyudaService ayudaService) {
        this.ayudaService = ayudaService;
    }

    @GetMapping("/preguntas")
    @Operation(summary = "Listar Preguntas Frecuentes", description = "Devuelve las preguntas frecuentes activas ordenadas por prioridad")
    public ResponseEntity<List<AyudaPreguntaResponse>> listarPreguntas() {
        return ResponseEntity.ok(ayudaService.listarPreguntas());
    }

    @GetMapping("/categorias")
    @Operation(summary = "Listar Categorías de Ayuda", description = "Devuelve las categorías de ayuda con iconos y colores")
    public ResponseEntity<List<AyudaCategoriaResponse>> listarCategorias() {
        return ResponseEntity.ok(ayudaService.listarCategorias());
    }
}
