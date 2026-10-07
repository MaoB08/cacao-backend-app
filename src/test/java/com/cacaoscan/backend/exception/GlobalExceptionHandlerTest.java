package com.cacaoscan.backend.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class GlobalExceptionHandlerTest {

    @Test
    void genericErrorDoesNotExposeInternalExceptionDetails() {
        var response = new GlobalExceptionHandler()
                .handleGenericException(new IllegalStateException("database password leaked"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("error_interno_del_servidor", response.getBody().get("error"));
        assertFalse(response.getBody().containsKey("detalles"));
        assertFalse(response.getBody().containsKey("tipo"));
        assertFalse(response.getBody().toString().contains("database password leaked"));
    }
}
