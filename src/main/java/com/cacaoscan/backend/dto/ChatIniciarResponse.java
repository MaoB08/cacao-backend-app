package com.cacaoscan.backend.dto;

import java.util.UUID;

public class ChatIniciarResponse {
    private UUID chatId;
    private String mensaje;

    public ChatIniciarResponse() {}

    public ChatIniciarResponse(UUID chatId, String mensaje) {
        this.chatId = chatId;
        this.mensaje = mensaje;
    }

    public UUID getChatId() { return chatId; }
    public void setChatId(UUID chatId) { this.chatId = chatId; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
}
