package com.cacaoscan.backend.controller;

import com.cacaoscan.backend.dto.PerfilResponse;
import com.cacaoscan.backend.dto.PerfilUpdateRequest;
import com.cacaoscan.backend.service.PerfilService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/perfil")
@Tag(name = "Perfil", description = "Endpoints para gestionar el perfil del agricultor")
public class PerfilController {

    private final PerfilService perfilService;

    public PerfilController(PerfilService perfilService) {
        this.perfilService = perfilService;
    }

    @GetMapping
    @Operation(summary = "Obtener Perfil", description = "Devuelve el perfil completo del usuario autenticado, incluyendo finca y estadísticas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Perfil obtenido exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    public ResponseEntity<PerfilResponse> getPerfil(Principal principal) {
        PerfilResponse perfil = perfilService.getPerfil(principal.getName());
        return ResponseEntity.ok(perfil);
    }

    @PatchMapping
    @Operation(summary = "Actualizar Perfil", description = "Actualiza parcialmente el perfil del usuario (todos los campos son opcionales)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Perfil actualizado exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    public ResponseEntity<PerfilResponse> updatePerfil(Principal principal,
                                                        @Valid @RequestBody PerfilUpdateRequest request) {
        PerfilResponse perfil = perfilService.updatePerfil(principal.getName(), request);
        return ResponseEntity.ok(perfil);
    }

    @PostMapping("/foto")
    @Operation(summary = "Subir Foto de Perfil", description = "Sube una foto de perfil (max 5MB, jpg/png/webp)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Foto subida exitosamente"),
            @ApiResponse(responseCode = "400", description = "Archivo inválido o muy grande"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    public ResponseEntity<Map<String, String>> uploadFoto(Principal principal,
                                                           @RequestParam("foto") MultipartFile foto) {
        String fotoUrl = perfilService.updateFoto(principal.getName(), foto);
        Map<String, String> response = new HashMap<>();
        response.put("fotoPerfil", fotoUrl);
        return ResponseEntity.ok(response);
    }
}
