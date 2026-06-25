package com.cacaoscan.backend.controller;

import com.cacaoscan.backend.dto.*;
import com.cacaoscan.backend.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Test
    public void testLogin_Success() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@cacao.com");
        request.setPassword("Password123");

        UserDto userDto = new UserDto(UUID.randomUUID(), "Test User", "test@cacao.com", com.cacaoscan.backend.model.Rol.AGRICULTOR);
        LoginResponse response = new LoginResponse("access", "refresh", userDto);

        when(authService.login(any(LoginRequest.class), anyString())).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access"))
                .andExpect(jsonPath("$.refreshToken").value("refresh"))
                .andExpect(jsonPath("$.user.email").value("test@cacao.com"));
    }

    @Test
    public void testLogin_BadCredentials() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@cacao.com");
        request.setPassword("WrongPassword");

        when(authService.login(any(LoginRequest.class), anyString())).thenThrow(new BadCredentialsException("credenciales_invalidas"));

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("credenciales_invalidas"));
    }

    @Test
    public void testLogin_Locked() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@cacao.com");
        request.setPassword("Password123");

        when(authService.login(any(LoginRequest.class), anyString())).thenThrow(new LockedException("300"));

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isLocked())
                .andExpect(jsonPath("$.error").value("cuenta_bloqueada"))
                .andExpect(jsonPath("$.retryAfter").value(300));
    }

    @Test
    public void testRegister_Conflict() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setNombre("Test User");
        request.setEmail("test@cacao.com");
        request.setPassword("Password123");
        request.setTelefono("3102345678");
        request.setDepartamento("Norte de Santander");
        request.setMunicipio("Ocaña");

        when(authService.register(any(RegisterRequest.class))).thenThrow(new IllegalArgumentException("email_ya_registrado"));

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("email_ya_registrado"));
    }
}
