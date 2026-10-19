package com.cacaoscan.backend.controller;

import com.cacaoscan.backend.dto.VisitaDTOs.*;
import com.cacaoscan.backend.service.VisitaTecnicaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.cacaoscan.backend.service.VisitaImagenService;
import com.cacaoscan.backend.service.VisitaImagenService.ArchivoImagenResponse;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/visitas")
@Tag(name = "Visitas Técnicas", description = "Gestión del ciclo de vida de visitas técnicas del agrónomo")
public class VisitaController {

    private final VisitaTecnicaService visitaTecnicaService;
    private final VisitaImagenService visitaImagenService;

    public VisitaController(VisitaTecnicaService visitaTecnicaService,
                            VisitaImagenService visitaImagenService) {
        this.visitaTecnicaService = visitaTecnicaService;
        this.visitaImagenService = visitaImagenService;
    }

    @PostMapping
    @Operation(summary = "Crear visita técnica",
               description = "Programa una nueva visita técnica a una finca asignada")
    public ResponseEntity<VisitaDetalleResponse> crearVisita(
            @Valid @RequestBody CrearVisitaRequest request,
            Principal principal) {
        validarPrincipal(principal);
        VisitaDetalleResponse response = visitaTecnicaService.crearVisita(request, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar visitas",
               description = "Lista las visitas del agrónomo autenticado con filtros opcionales")
    public ResponseEntity<Page<VisitaResumenResponse>> listarVisitas(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) UUID fincaId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Principal principal) {
        validarPrincipal(principal);
        Page<VisitaResumenResponse> visitas = visitaTecnicaService.listarVisitas(
                principal.getName(), estado, fincaId, desde, hasta,
                PageRequest.of(page, size, Sort.by("fechaProgramada").ascending()));
        return ResponseEntity.ok(visitas);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalle de visita",
               description = "Obtiene el detalle completo de una visita con acceso controlado")
    public ResponseEntity<VisitaDetalleResponse> obtenerDetalle(
            @PathVariable UUID id,
            Principal principal) {
        validarPrincipal(principal);
        return ResponseEntity.ok(visitaTecnicaService.obtenerDetalle(id, principal.getName()));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Reprogramar visita",
               description = "Cambia la fecha y/o datos de una visita PROGRAMADA")
    public ResponseEntity<VisitaDetalleResponse> reprogramarVisita(
            @PathVariable UUID id,
            @Valid @RequestBody ReprogramarVisitaRequest request,
            Principal principal) {
        validarPrincipal(principal);
        return ResponseEntity.ok(visitaTecnicaService.reprogramarVisita(id, request, principal.getName()));
    }

    @PostMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar visita",
               description = "Cancela una visita PROGRAMADA o EN_CURSO con motivo obligatorio")
    public ResponseEntity<VisitaDetalleResponse> cancelarVisita(
            @PathVariable UUID id,
            @Valid @RequestBody CancelarVisitaRequest request,
            Principal principal) {
        validarPrincipal(principal);
        return ResponseEntity.ok(visitaTecnicaService.cancelarVisita(id, request, principal.getName()));
    }

    @PostMapping("/{id}/iniciar")
    @Operation(summary = "Iniciar visita",
               description = "Cambia el estado a EN_CURSO y registra la hora real de inicio")
    public ResponseEntity<VisitaDetalleResponse> iniciarVisita(
            @PathVariable UUID id,
            Principal principal) {
        validarPrincipal(principal);
        return ResponseEntity.ok(visitaTecnicaService.iniciarVisita(id, principal.getName()));
    }

    @PostMapping("/{id}/finalizar")
    @Operation(summary = "Finalizar visita",
               description = "Completa la visita con observaciones, recomendaciones e informe")
    public ResponseEntity<VisitaDetalleResponse> finalizarVisita(
            @PathVariable UUID id,
            @RequestBody(required = false) FinalizarVisitaRequest request,
            Principal principal) {
        validarPrincipal(principal);
        return ResponseEntity.ok(visitaTecnicaService.finalizarVisita(id, request, principal.getName()));
    }

    @PostMapping(value = "/{id}/imagenes", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Subir evidencia fotográfica",
               description = "Sube una imagen vinculada a una visita en estado EN_CURSO y la almacena en ruta privada")
    public ResponseEntity<VisitaImagenResponse> subirImagen(
            @PathVariable UUID id,
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) UUID loteId,
            @RequestParam(required = false) String notas,
            Principal principal) {
        validarPrincipal(principal);
        VisitaImagenResponse response = visitaImagenService.subirImagen(id, file, loteId, notas, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}/imagenes")
    @Operation(summary = "Galería de imágenes de la visita",
               description = "Lista las evidencias fotográficas asociadas a la visita técnica")
    public ResponseEntity<List<VisitaImagenResponse>> listarImagenes(
            @PathVariable UUID id,
            Principal principal) {
        validarPrincipal(principal);
        return ResponseEntity.ok(visitaImagenService.listarImagenes(id, principal.getName()));
    }

    @GetMapping("/{id}/imagenes/{imgId}/archivo")
    @Operation(summary = "Obtener archivo binario de imagen",
               description = "Descarga de forma segura y autenticada el archivo físico privado de la imagen")
    public ResponseEntity<Resource> obtenerArchivo(
            @PathVariable UUID id,
            @PathVariable UUID imgId,
            Principal principal) {
        validarPrincipal(principal);
        ArchivoImagenResponse resp = visitaImagenService.obtenerArchivo(id, imgId, principal.getName());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, resp.getMediaType())
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(resp.getContentLength()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + imgId + "\"")
                .body(resp.getResource());
    }

    @PostMapping("/{id}/imagenes/{imgId}/analizar")
    @Operation(summary = "Analizar imagen con IA",
               description = "Solicita diagnóstico de moniliasis/enfermedad al servicio neuronal privado si está activo")
    public ResponseEntity<AnalisisImagenResponse> analizarImagen(
            @PathVariable UUID id,
            @PathVariable UUID imgId,
            Principal principal) {
        validarPrincipal(principal);
        return ResponseEntity.ok(visitaImagenService.analizarImagen(id, imgId, principal.getName()));
    }

    private void validarPrincipal(Principal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autorizado");
        }
    }
}
