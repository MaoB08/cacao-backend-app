package com.cacaoscan.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFound(ResourceNotFoundException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "recurso_no_encontrado");
        body.put("mensaje", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(InvalidPolygonException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidPolygon(InvalidPolygonException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "poligono_invalido");
        body.put("mensaje", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(BadCredentialsException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "credenciales_invalidas");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    @ExceptionHandler(org.springframework.security.authentication.DisabledException.class)
    public ResponseEntity<Map<String, Object>> handleDisabled(org.springframework.security.authentication.DisabledException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "cuenta_deshabilitada");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<Map<String, Object>> handleLocked(LockedException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "cuenta_bloqueada");
        try {
            long retryAfter = Long.parseLong(ex.getMessage());
            body.put("retryAfter", retryAfter);
        } catch (NumberFormatException e) {
            body.put("retryAfter", 900); // 15 minutos por defecto
        }
        return ResponseEntity.status(HttpStatus.LOCKED).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "validacion");
        
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());
                
        body.put("detalles", details);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        Map<String, Object> body = new HashMap<>();
        String message = ex.getMessage();
        body.put("error", message);

        if ("email_ya_registrado".equals(message)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
        }
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(com.cacaoscan.backend.service.ExpertoService.ExpertoNoDisponibleException.class)
    public ResponseEntity<Map<String, Object>> handleExpertoNoDisponible(
            com.cacaoscan.backend.service.ExpertoService.ExpertoNoDisponibleException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "experto_no_disponible");
        body.put("tiempoEspera", ex.getTiempoEspera());
        return ResponseEntity.status(HttpStatus.LOCKED).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        logger.error("Error no controlado detectado en el servidor: ", ex);
        Map<String, Object> body = new HashMap<>();
        body.put("error", "error_interno_del_servidor");
        body.put("detalles", ex.getMessage() != null ? ex.getMessage() : "Error desconocido en el servidor");
        body.put("tipo", ex.getClass().getSimpleName());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
