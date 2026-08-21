package com.cacaoscan.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Excepción lanzada cuando las coordenadas enviadas por el frontend
 * no forman un polígono válido (menos de 3 vértices, autocruzamiento, etc.).
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidPolygonException extends RuntimeException {

    public InvalidPolygonException(String message) {
        super(message);
    }
}
