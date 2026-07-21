package com.cacaoscan.backend.controller;

import com.cacaoscan.backend.dto.ChatIniciarRequest;
import com.cacaoscan.backend.dto.ChatIniciarResponse;
import com.cacaoscan.backend.service.ExpertoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/chat")
@Tag(name = "Chat", description = "Endpoints para gestionar chats con expertos")
public class ChatController {

    private final ExpertoService expertoService;

    public ChatController(ExpertoService expertoService) {
        this.expertoService = expertoService;
    }

    @PostMapping("/iniciar")
    @Operation(summary = "Iniciar Chat", description = "Crea un registro de chat entre el agricultor autenticado y un experto disponible")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Chat iniciado exitosamente"),
            @ApiResponse(responseCode = "423", description = "Experto no disponible en este momento")
    })
    public ResponseEntity<ChatIniciarResponse> iniciarChat(Principal principal,
                                                            @Valid @RequestBody ChatIniciarRequest request) {
        ChatIniciarResponse response = expertoService.iniciarChat(principal.getName(), request.getExpertoId());
        return ResponseEntity.ok(response);
    }
}
