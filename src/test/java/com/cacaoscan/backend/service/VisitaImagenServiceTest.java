package com.cacaoscan.backend.service;

import com.cacaoscan.backend.dto.VisitaDTOs.*;
import com.cacaoscan.backend.exception.ResourceNotFoundException;
import com.cacaoscan.backend.model.*;
import com.cacaoscan.backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class VisitaImagenServiceTest {

    private VisitaTecnicaRepository visitaRepository;
    private VisitaImagenRepository visitaImagenRepository;
    private LoteRepository loteRepository;
    private AgronomoFincaService agronomoFincaService;
    private DiagnosticoRepository diagnosticoRepository;
    private UsuarioRepository usuarioRepository;
    private VisitaImagenService service;

    private Usuario agronomo;
    private Usuario agricultor;
    private Finca finca;
    private Lote lote;
    private VisitaTecnica visitaEnCurso;
    private Path tempUploadDir;

    // Bytes reales de JPEG y PNG
    private static final byte[] VALID_JPEG_BYTES = new byte[] {
            (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0x00, 0x10, 0x4A, 0x46, 0x49, 0x46
    };
    private static final byte[] VALID_PNG_BYTES = new byte[] {
            (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00, 0x00
    };

    @BeforeEach
    void setUp() throws IOException {
        visitaRepository = mock(VisitaTecnicaRepository.class);
        visitaImagenRepository = mock(VisitaImagenRepository.class);
        loteRepository = mock(LoteRepository.class);
        agronomoFincaService = mock(AgronomoFincaService.class);
        diagnosticoRepository = mock(DiagnosticoRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);

        service = new VisitaImagenService(
                visitaRepository, visitaImagenRepository, loteRepository,
                agronomoFincaService, diagnosticoRepository, usuarioRepository
        );

        tempUploadDir = Files.createTempDirectory("test_uploads_visitas");
        ReflectionTestUtils.setField(service, "uploadsDir", tempUploadDir.toString());
        ReflectionTestUtils.setField(service, "baseUrl", "http://localhost:8080");
        ReflectionTestUtils.setField(service, "mlServiceUrl", "");

        // Setup datos comunes
        agronomo = new Usuario();
        agronomo.setId(UUID.randomUUID());
        agronomo.setEmail("agronomo@cacaoscan.com");
        agronomo.setRol(Rol.EXPERTO);

        agricultor = new Usuario();
        agricultor.setId(UUID.randomUUID());
        agricultor.setEmail("agricultor@cacaoscan.com");
        agricultor.setRol(Rol.AGRICULTOR);

        finca = new Finca();
        finca.setId(UUID.randomUUID());
        finca.setNombre("Finca El Paraiso");
        finca.setUsuario(agricultor);

        lote = new Lote();
        lote.setId(UUID.randomUUID());
        lote.setNombre("Lote 1");
        lote.setFinca(finca);

        visitaEnCurso = new VisitaTecnica();
        visitaEnCurso.setId(UUID.randomUUID());
        visitaEnCurso.setAgronomo(agronomo);
        visitaEnCurso.setAgricultor(agricultor);
        visitaEnCurso.setFinca(finca);
        visitaEnCurso.setEstado(EstadoVisita.EN_CURSO);
        visitaEnCurso.setFechaInicioReal(LocalDateTime.now());

        when(agronomoFincaService.obtenerAgronomo("agronomo@cacaoscan.com")).thenReturn(agronomo);
        when(usuarioRepository.findByIdentificador("agronomo@cacaoscan.com")).thenReturn(Optional.of(agronomo));
        when(usuarioRepository.findByIdentificador("agricultor@cacaoscan.com")).thenReturn(Optional.of(agricultor));
        when(visitaRepository.findById(visitaEnCurso.getId())).thenReturn(Optional.of(visitaEnCurso));
    }

    @Nested
    @DisplayName("Subida de imágenes")
    class SubidaImagenes {

        @Test
        @DisplayName("Sube imagen JPEG válida exitosamente vinculada a visita EN_CURSO")
        void subeImagenJpegValida() {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "foto.jpg", "image/jpeg", VALID_JPEG_BYTES);

            when(visitaImagenRepository.save(any(VisitaImagen.class))).thenAnswer(inv -> inv.getArgument(0));

            VisitaImagenResponse response = service.subirImagen(
                    visitaEnCurso.getId(), file, null, "Mazorca con sospecha de monilia", "agronomo@cacaoscan.com");

            assertNotNull(response);
            assertNotNull(response.getId());
            assertTrue(response.getImagenUrl().contains(visitaEnCurso.getId().toString()));
            assertEquals("Mazorca con sospecha de monilia", response.getNotas());
            verify(visitaImagenRepository).save(any(VisitaImagen.class));
        }

        @Test
        @DisplayName("Sube imagen PNG válida con lote específico")
        void subeImagenPngConLote() {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "fruto.png", "image/png", VALID_PNG_BYTES);

            when(loteRepository.findById(lote.getId())).thenReturn(Optional.of(lote));
            when(visitaImagenRepository.save(any(VisitaImagen.class))).thenAnswer(inv -> inv.getArgument(0));

            VisitaImagenResponse response = service.subirImagen(
                    visitaEnCurso.getId(), file, lote.getId(), "Detalle del pedúnculo", "agronomo@cacaoscan.com");

            assertNotNull(response);
            assertEquals(lote.getId(), response.getLoteId());
            assertEquals(lote.getNombre(), response.getLoteNombre());
        }

        @Test
        @DisplayName("Rechaza archivo vacío")
        void rechazaArchivoVacio() {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "empty.jpg", "image/jpeg", new byte[0]);

            assertThrows(IllegalArgumentException.class, () ->
                    service.subirImagen(visitaEnCurso.getId(), file, null, null, "agronomo@cacaoscan.com"));
        }

        @Test
        @DisplayName("Rechaza archivo con contenido no imagen o firma falsa")
        void rechazaFirmaInvalida() {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "falso.jpg", "image/jpeg", "Esto es texto no una imagen".getBytes());

            assertThrows(IllegalArgumentException.class, () ->
                    service.subirImagen(visitaEnCurso.getId(), file, null, null, "agronomo@cacaoscan.com"));
        }

        @Test
        @DisplayName("Rechaza subida si la visita no está EN_CURSO")
        void rechazaSiNoEstaEnCurso() {
            visitaEnCurso.setEstado(EstadoVisita.PROGRAMADA);
            MockMultipartFile file = new MockMultipartFile(
                    "file", "foto.jpg", "image/jpeg", VALID_JPEG_BYTES);

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                    service.subirImagen(visitaEnCurso.getId(), file, null, null, "agronomo@cacaoscan.com"));
            assertTrue(ex.getMessage().contains("EN_CURSO"));
        }

        @Test
        @DisplayName("Rechaza subida si la visita pertenece a otro agrónomo")
        void rechazaSiPerteneceAOtroAgronomo() {
            Usuario otroAgronomo = new Usuario();
            otroAgronomo.setId(UUID.randomUUID());
            visitaEnCurso.setAgronomo(otroAgronomo);

            MockMultipartFile file = new MockMultipartFile(
                    "file", "foto.jpg", "image/jpeg", VALID_JPEG_BYTES);

            assertThrows(IllegalArgumentException.class, () ->
                    service.subirImagen(visitaEnCurso.getId(), file, null, null, "agronomo@cacaoscan.com"));
        }

        @Test
        @DisplayName("Rechaza si el lote no pertenece a la finca de la visita")
        void rechazaLoteDeOtraFinca() {
            Finca otraFinca = new Finca();
            otraFinca.setId(UUID.randomUUID());
            Lote loteAjeno = new Lote();
            loteAjeno.setId(UUID.randomUUID());
            loteAjeno.setFinca(otraFinca);

            when(loteRepository.findById(loteAjeno.getId())).thenReturn(Optional.of(loteAjeno));

            MockMultipartFile file = new MockMultipartFile(
                    "file", "foto.jpg", "image/jpeg", VALID_JPEG_BYTES);

            assertThrows(IllegalArgumentException.class, () ->
                    service.subirImagen(visitaEnCurso.getId(), file, loteAjeno.getId(), null, "agronomo@cacaoscan.com"));
        }
    }

    @Nested
    @DisplayName("Galería y Descarga")
    class GaleriaYDescarga {

        @Test
        @DisplayName("Lista galería de imágenes de la visita")
        void listaGaleria() {
            VisitaImagen img1 = new VisitaImagen();
            img1.setId(UUID.randomUUID());
            img1.setVisita(visitaEnCurso);
            img1.setImagenUrl("http://localhost:8080/img1");

            when(visitaImagenRepository.findByVisitaOrderByCreatedAtDesc(visitaEnCurso))
                    .thenReturn(List.of(img1));

            List<VisitaImagenResponse> list = service.listarImagenes(visitaEnCurso.getId(), "agronomo@cacaoscan.com");

            assertEquals(1, list.size());
            assertEquals(img1.getId(), list.get(0).getId());
        }

        @Test
        @DisplayName("Usuario ajeno recibe 403 al intentar acceder a la galería")
        void usuarioAjenoRecibe403() {
            Usuario ajeno = new Usuario();
            ajeno.setId(UUID.randomUUID());
            ajeno.setEmail("ajeno@cacaoscan.com");

            when(usuarioRepository.findByIdentificador("ajeno@cacaoscan.com")).thenReturn(Optional.of(ajeno));

            assertThrows(ResponseStatusException.class, () ->
                    service.listarImagenes(visitaEnCurso.getId(), "ajeno@cacaoscan.com"));
        }
    }

    @Nested
    @DisplayName("Análisis IA (MA-17)")
    class AnalisisIA {

        @Test
        @DisplayName("Cuando el servicio ML no está configurado, responde status no_disponible verídicamente")
        void respondeNoDisponibleVeridico() {
            VisitaImagen img = new VisitaImagen();
            img.setId(UUID.randomUUID());
            img.setVisita(visitaEnCurso);
            img.setImagenUrl("http://localhost:8080/img");

            when(visitaImagenRepository.findById(img.getId())).thenReturn(Optional.of(img));

            AnalisisImagenResponse resp = service.analizarImagen(
                    visitaEnCurso.getId(), img.getId(), "agronomo@cacaoscan.com");

            assertNotNull(resp);
            assertEquals("no_disponible", resp.getStatus());
            assertTrue(resp.getMensaje().contains("no disponible"));
            assertEquals(img.getId(), resp.getImagenId());
        }

        @Test
        @DisplayName("Rechaza análisis si la imagen no pertenece a la visita")
        void rechazaImagenDeOtraVisita() {
            VisitaTecnica otraVisita = new VisitaTecnica();
            otraVisita.setId(UUID.randomUUID());

            VisitaImagen imgAjena = new VisitaImagen();
            imgAjena.setId(UUID.randomUUID());
            imgAjena.setVisita(otraVisita);

            when(visitaImagenRepository.findById(imgAjena.getId())).thenReturn(Optional.of(imgAjena));

            assertThrows(IllegalArgumentException.class, () ->
                    service.analizarImagen(visitaEnCurso.getId(), imgAjena.getId(), "agronomo@cacaoscan.com"));
        }
    }
}
