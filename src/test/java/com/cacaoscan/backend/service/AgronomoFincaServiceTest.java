package com.cacaoscan.backend.service;

import com.cacaoscan.backend.exception.ResourceNotFoundException;
import com.cacaoscan.backend.model.*;
import com.cacaoscan.backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AgronomoFincaServiceTest {

    private AgronomoFincaRepository agronomoFincaRepository;
    private FincaRepository fincaRepository;
    private UsuarioRepository usuarioRepository;
    private AgronomoFincaService service;

    private Usuario agronomo;
    private Usuario agricultor;
    private Finca finca;

    @BeforeEach
    void setUp() {
        agronomoFincaRepository = mock(AgronomoFincaRepository.class);
        fincaRepository = mock(FincaRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);
        service = new AgronomoFincaService(agronomoFincaRepository, fincaRepository, usuarioRepository);

        agronomo = new Usuario();
        agronomo.setId(UUID.randomUUID());
        agronomo.setNombre("Agrónomo Test");
        agronomo.setRol(Rol.EXPERTO);

        agricultor = new Usuario();
        agricultor.setId(UUID.randomUUID());
        agricultor.setNombre("Agricultor Test");
        agricultor.setRol(Rol.AGRICULTOR);

        finca = new Finca();
        finca.setId(UUID.randomUUID());
        finca.setNombre("Finca Test");
        finca.setUsuario(agricultor);
        finca.setActiva(true);
    }

    @Test
    @DisplayName("obtenerAgronomo rechaza AGRICULTOR")
    void rechazaAgricultor() {
        when(usuarioRepository.findByIdentificador("agricultor@test.com"))
                .thenReturn(Optional.of(agricultor));

        assertThrows(IllegalArgumentException.class,
                () -> service.obtenerAgronomo("agricultor@test.com"));
    }

    @Test
    @DisplayName("obtenerAgronomo rechaza usuario inexistente")
    void rechazaInexistente() {
        when(usuarioRepository.findByIdentificador("fantasma@test.com"))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.obtenerAgronomo("fantasma@test.com"));
    }

    @Test
    @DisplayName("obtenerAgronomo acepta EXPERTO")
    void aceptaExperto() {
        when(usuarioRepository.findByIdentificador("agronomo@test.com"))
                .thenReturn(Optional.of(agronomo));

        Usuario result = service.obtenerAgronomo("agronomo@test.com");
        assertEquals(agronomo.getId(), result.getId());
    }

    @Test
    @DisplayName("validarAsignacion rechaza finca no asignada")
    void rechazaNoAsignada() {
        when(agronomoFincaRepository.existsByAgronomoAndFincaAndActivaTrue(agronomo, finca))
                .thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> service.validarAsignacion(agronomo, finca));
    }

    @Test
    @DisplayName("validarAsignacion acepta finca asignada")
    void aceptaAsignada() {
        when(agronomoFincaRepository.existsByAgronomoAndFincaAndActivaTrue(agronomo, finca))
                .thenReturn(true);

        assertDoesNotThrow(() -> service.validarAsignacion(agronomo, finca));
    }

    @Test
    @DisplayName("obtenerDetalleFinca rechaza finca inexistente")
    void detalleRechazaInexistente() {
        when(usuarioRepository.findByIdentificador("agronomo@test.com"))
                .thenReturn(Optional.of(agronomo));
        when(fincaRepository.findById(any(UUID.class)))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.obtenerDetalleFinca(UUID.randomUUID(), "agronomo@test.com"));
    }

    @Test
    @DisplayName("obtenerDetalleFinca rechaza finca no asignada")
    void detalleRechazaNoAsignada() {
        when(usuarioRepository.findByIdentificador("agronomo@test.com"))
                .thenReturn(Optional.of(agronomo));
        when(fincaRepository.findById(finca.getId()))
                .thenReturn(Optional.of(finca));
        when(agronomoFincaRepository.existsByAgronomoAndFincaAndActivaTrue(agronomo, finca))
                .thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> service.obtenerDetalleFinca(finca.getId(), "agronomo@test.com"));
    }
}
