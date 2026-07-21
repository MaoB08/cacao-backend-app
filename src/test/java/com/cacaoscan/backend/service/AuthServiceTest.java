package com.cacaoscan.backend.service;

import com.cacaoscan.backend.dto.*;
import com.cacaoscan.backend.model.Rol;
import com.cacaoscan.backend.model.Usuario;
import com.cacaoscan.backend.model.RefreshToken;
import com.cacaoscan.backend.repository.UsuarioRepository;
import com.cacaoscan.backend.repository.RefreshTokenRepository;
import com.cacaoscan.backend.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private RateLimiterService rateLimiterService;
    @Mock private RecoveryTokenService recoveryTokenService;
    @Mock private EmailService emailService;
    @Mock private org.springframework.data.redis.core.StringRedisTemplate redisTemplate;

    @InjectMocks
    private AuthService authService;

    private Usuario usuario;
    private String email = "juan.valdez@cacao.com";
    private String password = "Password123";
    private String ip = "192.168.1.1";

    @BeforeEach
    public void setup() {
        usuario = new Usuario();
        usuario.setId(UUID.randomUUID());
        usuario.setNombre("Juan Valdez");
        usuario.setEmail(email);
        usuario.setPasswordHash("hashed_password");
        usuario.setRol(Rol.AGRICULTOR);
        usuario.setActivo(true);
    }

    @Test
    public void testLogin_Success() {
        LoginRequest request = new LoginRequest();
        request.setEmail(email);
        request.setPassword(password);

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn(email);
        when(rateLimiterService.isBlocked(email, ip)).thenReturn(false);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));
        when(jwtTokenProvider.generateAccessToken(auth)).thenReturn("access_token");

        LoginResponse response = authService.login(request, ip);

        assertNotNull(response);
        assertEquals("access_token", response.getAccessToken());
        assertEquals(email, response.getUser().getEmail());
        verify(rateLimiterService).resetAttempts(email, ip);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    public void testLogin_Blocked() {
        LoginRequest request = new LoginRequest();
        request.setEmail(email);
        request.setPassword(password);

        when(rateLimiterService.isBlocked(email, ip)).thenReturn(true);
        when(rateLimiterService.getBlockedTimeRemaining(email, ip)).thenReturn(600L);

        assertThrows(LockedException.class, () -> authService.login(request, ip));
        verify(authenticationManager, never()).authenticate(any());
    }

    @Test
    public void testLogin_BadCredentials() {
        LoginRequest request = new LoginRequest();
        request.setEmail(email);
        request.setPassword(password);

        when(rateLimiterService.isBlocked(email, ip)).thenReturn(false);
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad_credentials"));

        assertThrows(BadCredentialsException.class, () -> authService.login(request, ip));
        verify(rateLimiterService).recordFailedAttempt(email, ip);
    }

    @Test
    public void testRegister_Success() {
        RegisterRequest request = new RegisterRequest();
        request.setNombre("Juan Valdez");
        request.setEmail(email);
        request.setPassword(password);
        request.setTelefono("3102345678");
        request.setDepartamento("Norte de Santander");
        request.setMunicipio("Ocaña");

        when(usuarioRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn("hashed_password");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
        when(jwtTokenProvider.generateAccessTokenFromUsername(email)).thenReturn("access_token");

        RegisterResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("access_token", response.getAccessToken());
        verify(usuarioRepository).save(any(Usuario.class));
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    public void testRegister_EmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail(email);

        when(usuarioRepository.existsByEmail(email)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> authService.register(request));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    public void testRefresh_Success() {
        String rawToken = "raw_refresh_token";
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken(rawToken);

        RefreshToken token = new RefreshToken();
        token.setUsuario(usuario);
        token.setExpiryDate(LocalDateTime.now().plusDays(10));

        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(token));
        when(jwtTokenProvider.generateAccessTokenFromUsername(email)).thenReturn("new_access_token");

        RefreshTokenResponse response = authService.refresh(request);

        assertNotNull(response);
        assertEquals("new_access_token", response.getAccessToken());
        verify(refreshTokenRepository).delete(token);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    public void testResetPassword_Success() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("recovery_token");
        request.setNuevaPassword("NewPassword123");

        UUID userId = usuario.getId();
        when(recoveryTokenService.getUserIdByToken("recovery_token")).thenReturn(userId);
        when(usuarioRepository.findById(userId)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.encode("NewPassword123")).thenReturn("new_hashed_password");

        authService.resetPassword(request);

        verify(usuarioRepository).save(usuario);
        verify(refreshTokenRepository).deleteByUsuarioId(userId);
        verify(recoveryTokenService).invalidateToken("recovery_token");
    }
}
