package com.cacaoscan.backend.service;

import com.cacaoscan.backend.dto.*;
import com.cacaoscan.backend.model.Rol;
import com.cacaoscan.backend.model.Usuario;
import com.cacaoscan.backend.model.RefreshToken;
import com.cacaoscan.backend.repository.UsuarioRepository;
import com.cacaoscan.backend.repository.RefreshTokenRepository;
import com.cacaoscan.backend.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final RateLimiterService rateLimiterService;
    private final RecoveryTokenService recoveryTokenService;
    private final EmailService emailService;

    @Value("${app.jwt.refreshExpirationMs}")
    private long jwtRefreshExpirationMs;

    public AuthService(AuthenticationManager authenticationManager,
                       UsuarioRepository usuarioRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       JwtTokenProvider jwtTokenProvider,
                       PasswordEncoder passwordEncoder,
                       RateLimiterService rateLimiterService,
                       RecoveryTokenService recoveryTokenService,
                       EmailService emailService) {
        this.authenticationManager = authenticationManager;
        this.usuarioRepository = usuarioRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
        this.rateLimiterService = rateLimiterService;
        this.recoveryTokenService = recoveryTokenService;
        this.emailService = emailService;
    }

    /**
     * Inicia sesión del usuario, validando credenciales y rate limits.
     */
    @Transactional
    public LoginResponse login(LoginRequest request, String ip) {
        String email = request.getEmail().toLowerCase().trim();

        // 1. Verificar si está bloqueado por Rate Limiting
        if (rateLimiterService.isBlocked(email, ip)) {
            long retryAfter = rateLimiterService.getBlockedTimeRemaining(email, ip);
            throw new LockedException(String.valueOf(retryAfter));
        }

        try {
            // 2. Autenticar credenciales
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.getPassword())
            );

            // 3. Si tiene éxito, resetear intentos fallidos
            rateLimiterService.resetAttempts(email, ip);

            // 4. Obtener usuario de la base de datos
            Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));

            // 5. Generar Access Token
            String accessToken = jwtTokenProvider.generateAccessToken(authentication);

            // 6. Generar y hashear el Refresh Token
            String rawRefreshToken = UUID.randomUUID().toString();
            String tokenHash = sha256(rawRefreshToken);

            // Limpiar refresh tokens anteriores del usuario
            refreshTokenRepository.deleteByUsuario(usuario);

            // Guardar el nuevo refresh token
            RefreshToken refreshToken = new RefreshToken();
            refreshToken.setUsuario(usuario);
            refreshToken.setTokenHash(tokenHash);
            refreshToken.setExpiryDate(LocalDateTime.now().plus(jwtRefreshExpirationMs, java.time.temporal.ChronoUnit.MILLIS));
            refreshTokenRepository.save(refreshToken);

            UserDto userDto = new UserDto(usuario.getId(), usuario.getNombre(), usuario.getEmail(), usuario.getRol(), usuario.getTelefono(), usuario.getDepartamento(), usuario.getMunicipio(), usuario.getNombreFinca());
            return new LoginResponse(accessToken, rawRefreshToken, userDto);

        } catch (AuthenticationException e) {
            // 7. Si falla, registrar el intento fallido
            rateLimiterService.recordFailedAttempt(email, ip);
            throw new BadCredentialsException("credenciales_invalidas");
        }
    }

    /**
     * Registra un nuevo agricultor e inicia sesión automáticamente.
     */
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String email = request.getEmail().toLowerCase().trim();

        // Verificar si el correo ya está registrado
        if (usuarioRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("email_ya_registrado");
        }

        // Crear nueva entidad Usuario
        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre());
        usuario.setEmail(email);
        usuario.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        usuario.setTelefono(request.getTelefono());
        usuario.setDepartamento(request.getDepartamento());
        usuario.setMunicipio(request.getMunicipio());
        usuario.setNombreFinca(request.getNombreFinca());
        usuario.setRol(Rol.AGRICULTOR);
        usuario.setActivo(true);
        usuario.setEmailVerificado(false);

        usuario = usuarioRepository.save(usuario);

        // Generar tokens para inicio de sesión automático
        String accessToken = jwtTokenProvider.generateAccessTokenFromUsername(usuario.getEmail());
        String rawRefreshToken = UUID.randomUUID().toString();
        String tokenHash = sha256(rawRefreshToken);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUsuario(usuario);
        refreshToken.setTokenHash(tokenHash);
        refreshToken.setExpiryDate(LocalDateTime.now().plus(jwtRefreshExpirationMs, java.time.temporal.ChronoUnit.MILLIS));
        refreshTokenRepository.save(refreshToken);

        UserDto userDto = new UserDto(usuario.getId(), usuario.getNombre(), usuario.getEmail(), usuario.getRol(), usuario.getTelefono(), usuario.getDepartamento(), usuario.getMunicipio(), usuario.getNombreFinca());
        return new RegisterResponse(accessToken, rawRefreshToken, userDto);
    }

    /**
     * Renueva el Access Token utilizando un Refresh Token válido.
     */
    @Transactional
    public RefreshTokenResponse refresh(RefreshTokenRequest request) {
        String rawToken = request.getRefreshToken();
        String hash = sha256(rawToken);

        // Buscar el refresh token en la base de datos
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new IllegalArgumentException("token_invalido_o_expirado"));

        // Validar expiración del refresh token
        if (refreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new IllegalArgumentException("token_invalido_o_expirado");
        }

        Usuario usuario = refreshToken.getUsuario();

        // Generar nuevo Access Token y Refresh Token (Rotación de tokens)
        String newAccessToken = jwtTokenProvider.generateAccessTokenFromUsername(usuario.getEmail());
        String newRawRefreshToken = UUID.randomUUID().toString();
        String newHash = sha256(newRawRefreshToken);

        // Reemplazar el refresh token anterior
        refreshTokenRepository.delete(refreshToken);

        RefreshToken newRefreshToken = new RefreshToken();
        newRefreshToken.setUsuario(usuario);
        newRefreshToken.setTokenHash(newHash);
        newRefreshToken.setExpiryDate(LocalDateTime.now().plus(jwtRefreshExpirationMs, java.time.temporal.ChronoUnit.MILLIS));
        refreshTokenRepository.save(newRefreshToken);

        return new RefreshTokenResponse(newAccessToken, newRawRefreshToken);
    }

    /**
     * Gestiona la solicitud de olvido de contraseña de forma segura.
     */
    public void forgotPassword(ForgotPasswordRequest request) {
        String email = request.getEmail().toLowerCase().trim();
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);

        // No revelamos si el email existe o no por seguridad, simplemente respondemos 200.
        if (usuarioOpt.isPresent() && usuarioOpt.get().isActivo()) {
            Usuario usuario = usuarioOpt.get();
            // Crear token UUID temporal en Redis con TTL de 30 mins
            String token = recoveryTokenService.createRecoveryToken(usuario.getId());
            // Enviar correo local
            emailService.sendRecoveryEmail(usuario.getEmail(), token);
        }
    }

    /**
     * Restablece la contraseña de un usuario usando el token temporal de Redis.
     */
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        String token = request.getToken();
        UUID userId = recoveryTokenService.getUserIdByToken(token);

        if (userId == null) {
            throw new IllegalArgumentException("token_invalido_o_expirado");
        }

        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("token_invalido_o_expirado"));

        // Actualizar contraseña del usuario
        usuario.setPasswordHash(passwordEncoder.encode(request.getNuevaPassword()));
        usuarioRepository.save(usuario);

        // Invalidar todos los refresh tokens del usuario para forzar re-login
        refreshTokenRepository.deleteByUsuarioId(usuario.getId());

        // Invalidar el token de recuperación en Redis
        recoveryTokenService.invalidateToken(token);
    }

    /**
     * Función utilitaria para hashear tokens de forma segura con SHA-256.
     */
    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error al calcular hash SHA-256", e);
        }
    }
}
