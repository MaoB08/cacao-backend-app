package com.cacaoscan.backend.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class ChatIniciarRequest {
    @NotNull(message = "El ID del experto es obligatorio")
    private UUID expertoId;

    public UUID getExpertoId() { return expertoId; }
    public void setExpertoId(UUID expertoId) { this.expertoId = expertoId; }
}
