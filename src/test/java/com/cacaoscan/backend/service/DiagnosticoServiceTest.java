package com.cacaoscan.backend.service;

import com.cacaoscan.backend.dto.DiagnosticoDTOs.*;
import com.cacaoscan.backend.model.Diagnostico;
import com.cacaoscan.backend.model.Usuario;
import com.cacaoscan.backend.repository.DiagnosticoRepository;
import com.cacaoscan.backend.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiagnosticoServiceTest {

    @Mock
    private DiagnosticoRepository diagnosticoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private MlServiceClient mlServiceClient;

    private ObjectMapper objectMapper;
    private DiagnosticoService diagnosticoService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        diagnosticoService = new DiagnosticoService(
                diagnosticoRepository,
                usuarioRepository,
                objectMapper,
                mlServiceClient
        );
    }

    @Test
    @DisplayName("procesarAnalisisIA exitoso con predicción monilia_m1")
    void testProcesarAnalisisIaExitoso() {
        MockMultipartFile file = new MockMultipartFile(
                "imagen",
                "mazorca_test.jpg",
                "image/jpeg",
                new byte[]{1, 2, 3, 4}
        );

        MlPredictionResponse mlResponse = new MlPredictionResponse();
        mlResponse.setRequestId("req-123");
        mlResponse.setModelVersion("monilia-efficientnet_b0-0.1.0");
        mlResponse.setStatus("ok");

        MlPredictionItem top = new MlPredictionItem();
        top.setClassCode("monilia_m1");
        top.setLabel("Moniliasis etapa inicial");
        top.setProbability(0.875);
        mlResponse.setTopPrediction(top);
        mlResponse.setPredictions(List.of(top));

        when(mlServiceClient.predict(any(), anyString())).thenReturn(mlResponse);

        DiagnosticoResponse response = diagnosticoService.procesarAnalisisIA(file, "agricultor@cultivia.com");

        assertNotNull(response);
        assertEquals("monilia_m1", response.getCodigoClase());
        assertEquals(0.875, response.getProbabilidad());
        assertEquals("87.5%", response.getConfianza());
        assertEquals("monilia-efficientnet_b0-0.1.0", response.getModelVersion());
        assertTrue(response.isTieneEnfermedad());
        assertTrue(response.isConcluyente());
        assertEquals("Moniliasis — Etapa inicial", response.getNombreEnfermedad());
        assertFalse(response.getRecomendaciones().isEmpty());
    }

    @Test
    @DisplayName("procesarAnalisisIA con mazorca sana")
    void testProcesarAnalisisIaSana() {
        MockMultipartFile file = new MockMultipartFile(
                "imagen",
                "sana.png",
                "image/png",
                new byte[]{10, 20}
        );

        MlPredictionResponse mlResponse = new MlPredictionResponse();
        mlResponse.setRequestId("req-456");
        mlResponse.setModelVersion("monilia-efficientnet_b0-0.1.0");
        mlResponse.setStatus("ok");

        MlPredictionItem top = new MlPredictionItem();
        top.setClassCode("healthy");
        top.setLabel("Mazorca sana");
        top.setProbability(0.962);
        mlResponse.setTopPrediction(top);
        mlResponse.setPredictions(List.of(top));

        when(mlServiceClient.predict(any(), anyString())).thenReturn(mlResponse);

        DiagnosticoResponse response = diagnosticoService.procesarAnalisisIA(file, "agricultor@cultivia.com");

        assertNotNull(response);
        assertEquals("healthy", response.getCodigoClase());
        assertFalse(response.isTieneEnfermedad());
        assertEquals("Mazorca Sana", response.getNombreEnfermedad());
        assertEquals("Nula", response.getSeveridad());
    }

    @Test
    @DisplayName("procesarAnalisisIA rechaza archivo vacío")
    void testRechazaArchivoVacio() {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "imagen",
                "empty.jpg",
                "image/jpeg",
                new byte[0]
        );

        assertThrows(ResponseStatusException.class, () ->
                diagnosticoService.procesarAnalisisIA(emptyFile, "user@test.com"));
        verifyNoInteractions(mlServiceClient);
    }

    @Test
    @DisplayName("guardarDiagnostico persiste campos de trazabilidad ML")
    void testGuardarDiagnostico() {
        Usuario usuario = new Usuario();
        usuario.setId(UUID.randomUUID());
        usuario.setEmail("user@test.com");

        when(usuarioRepository.findByIdentificador("user@test.com")).thenReturn(Optional.of(usuario));
        when(diagnosticoRepository.save(any(Diagnostico.class))).thenAnswer(invocation -> {
            Diagnostico d = invocation.getArgument(0);
            d.setId(UUID.randomUUID());
            return d;
        });

        DiagnosticoResponse dto = new DiagnosticoResponse();
        dto.setCodigoClase("monilia_m2");
        dto.setProbabilidad(0.82);
        dto.setModelVersion("monilia-efficientnet_b0-0.1.0");
        dto.setConcluyente(true);
        dto.setTieneEnfermedad(true);
        dto.setNombreEnfermedad("Moniliasis — Etapa intermedia");
        dto.setConfianza("82.0%");
        dto.setSeveridad("Media");
        dto.setRecomendaciones(List.of(new RecomendacionItem("1", "rojo", "Retirar", "Corte el fruto")));

        DiagnosticoResponse saved = diagnosticoService.guardarDiagnostico(dto, "user@test.com");

        assertNotNull(saved);
        assertEquals("monilia_m2", saved.getCodigoClase());
        assertEquals(0.82, saved.getProbabilidad());
        assertEquals("monilia-efficientnet_b0-0.1.0", saved.getModelVersion());
        verify(diagnosticoRepository).save(any(Diagnostico.class));
    }
}
