package com.cacaoscan.backend.controller;

import com.cacaoscan.backend.dto.*;
import com.cacaoscan.backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticación", description = "Endpoints para el registro, inicio de sesión y recuperación de agricultores")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar Sesión", description = "Autentica al usuario con email y contraseña, devolviendo tokens JWT de acceso y refresco")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Autenticación exitosa"),
            @ApiResponse(responseCode = "401", description = "Credenciales incorrectas"),
            @ApiResponse(responseCode = "423", description = "Cuenta bloqueada temporalmente por exceso de intentos fallidos")
    })
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        String ip = getClientIp(request);
        LoginResponse response = authService.login(loginRequest, ip);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    @Operation(summary = "Registrar Agricultor", description = "Crea una nueva cuenta de agricultor en el sistema y lo autentica automáticamente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Registro exitoso"),
            @ApiResponse(responseCode = "400", description = "Error de validación en los campos"),
            @ApiResponse(responseCode = "409", description = "El correo electrónico ya está registrado")
    })
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        RegisterResponse response = authService.register(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Renovar Token", description = "Genera un nuevo Access Token y rota el Refresh Token usando un Refresh Token válido")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tokens renovados exitosamente"),
            @ApiResponse(responseCode = "400", description = "Refresh Token inválido o expirado")
    })
    public ResponseEntity<RefreshTokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        RefreshTokenResponse response = authService.refresh(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Solicitar Recuperación", description = "Envía un correo con un enlace/token temporal para restablecer la contraseña (respuesta silenciosa por seguridad)")
    @ApiResponse(responseCode = "200", description = "Si el correo existe, se enviará el enlace de recuperación")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        Map<String, String> response = new HashMap<>();
        response.put("mensaje", "Si el correo existe, se enviará un enlace");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Restablecer Contraseña", description = "Actualiza la contraseña del usuario utilizando el token temporal recibido por correo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contraseña actualizada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Token de recuperación inválido o expirado")
    })
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        Map<String, String> response = new HashMap<>();
        response.put("mensaje", "Contraseña actualizada");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @Operation(summary = "Cerrar Sesión", description = "Invalida el refresh token en PostgreSQL y añade el access token a la blacklist de Redis")
    @ApiResponse(responseCode = "200", description = "Sesión cerrada exitosamente")
    public ResponseEntity<Map<String, String>> logout(@Valid @RequestBody LogoutRequest logoutRequest,
                                                       HttpServletRequest request) {
        // Extraer el access token del header Authorization
        String authHeader = request.getHeader("Authorization");
        String accessToken = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            accessToken = authHeader.substring(7);
        }
        authService.logout(logoutRequest.getRefreshToken(), accessToken);
        Map<String, String> response = new HashMap<>();
        response.put("mensaje", "Sesión cerrada");
        return ResponseEntity.ok(response);
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
