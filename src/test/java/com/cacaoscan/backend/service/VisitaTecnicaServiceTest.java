package com.cacaoscan.backend.service;

import com.cacaoscan.backend.dto.VisitaDTOs.*;
import com.cacaoscan.backend.exception.ResourceNotFoundException;
import com.cacaoscan.backend.model.*;
import com.cacaoscan.backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class VisitaTecnicaServiceTest {

    private VisitaTecnicaRepository visitaRepository;
    private FincaRepository fincaRepository;
    private LoteRepository loteRepository;
    private UsuarioRepository usuarioRepository;
    private AgronomoFincaRepository agronomoFincaRepository;
    private VisitaImagenRepository visitaImagenRepository;

    private AgronomoFincaService agronomoFincaService;
    private VisitaTecnicaService service;

    private Usuario agronomo;
    private Usuario agricultor;
    private Finca finca;
    private Lote lote;

    @BeforeEach
    void setUp() {
        visitaRepository = mock(VisitaTecnicaRepository.class);
        fincaRepository = mock(FincaRepository.class);
        loteRepository = mock(LoteRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);
        agronomoFincaRepository = mock(AgronomoFincaRepository.class);
        visitaImagenRepository = mock(VisitaImagenRepository.class);

        agronomoFincaService = new AgronomoFincaService(
                agronomoFincaRepository, fincaRepository, usuarioRepository);
        service = new VisitaTecnicaService(
                visitaRepository, fincaRepository, loteRepository,
                usuarioRepository, agronomoFincaService, visitaImagenRepository);

        // Setup common entities
        agronomo = new Usuario();
        agronomo.setId(UUID.randomUUID());
        agronomo.setNombre("Dr. Agrónomo Test");
        agronomo.setRol(Rol.EXPERTO);

        agricultor = new Usuario();
        agricultor.setId(UUID.randomUUID());
        agricultor.setNombre("Agricultor Test");
        agricultor.setRol(Rol.AGRICULTOR);

        finca = new Finca();
        finca.setId(UUID.randomUUID());
        finca.setNombre("Finca La Esperanza");
        finca.setMunicipio("San Vicente");
        finca.setDepartamento("Santander");
        finca.setUsuario(agricultor);
        finca.setActiva(true);

        lote = new Lote();
        lote.setId(UUID.randomUUID());
        lote.setNombre("Lote A");
        lote.setVariedadCacao("CCN-51");
        lote.setFinca(finca);
    }

    // ======================== Autorización ========================

    @Nested
    @DisplayName("Autorización por rol")
    class AutorizacionRol {

        @Test
        @DisplayName("Un AGRICULTOR no puede crear visitas")
        void agricultorNoPuedeCrearVisita() {
            Usuario noExperto = new Usuario();
            noExperto.setId(UUID.randomUUID());
            noExperto.setRol(Rol.AGRICULTOR);

            when(usuarioRepository.findByIdentificador("agricultor@test.com"))
                    .thenReturn(Optional.of(noExperto));

            CrearVisitaRequest request = new CrearVisitaRequest();
            request.setFincaId(finca.getId());
            request.setFechaProgramada(LocalDateTime.now().plusDays(1));

            assertThrows(IllegalArgumentException.class,
                    () -> service.crearVisita(request, "agricultor@test.com"));
        }

        @Test
        @DisplayName("Un ADMIN no puede crear visitas como agrónomo")
        void adminNoPuedeCrearVisita() {
            Usuario admin = new Usuario();
            admin.setId(UUID.randomUUID());
            admin.setRol(Rol.ADMIN);

            when(usuarioRepository.findByIdentificador("admin@test.com"))
                    .thenReturn(Optional.of(admin));

            CrearVisitaRequest request = new CrearVisitaRequest();
            request.setFincaId(finca.getId());
            request.setFechaProgramada(LocalDateTime.now().plusDays(1));

            assertThrows(IllegalArgumentException.class,
                    () -> service.crearVisita(request, "admin@test.com"));
        }
    }

    // ======================== Asignación de finca ========================

    @Nested
    @DisplayName("Asignación agrónomo ↔ finca")
    class AsignacionFinca {

        @Test
        @DisplayName("Rechaza crear visita en finca no asignada")
        void rechazaFincaNoAsignada() {
            when(usuarioRepository.findByIdentificador("agronomo@test.com"))
                    .thenReturn(Optional.of(agronomo));
            when(fincaRepository.findById(finca.getId()))
                    .thenReturn(Optional.of(finca));
            when(agronomoFincaRepository.existsByAgronomoAndFincaAndActivaTrue(agronomo, finca))
                    .thenReturn(false);

            CrearVisitaRequest request = new CrearVisitaRequest();
            request.setFincaId(finca.getId());
            request.setFechaProgramada(LocalDateTime.now().plusDays(1));

            assertThrows(IllegalArgumentException.class,
                    () -> service.crearVisita(request, "agronomo@test.com"));
        }

        @Test
        @DisplayName("Rechaza finca inexistente")
        void rechazaFincaInexistente() {
            when(usuarioRepository.findByIdentificador("agronomo@test.com"))
                    .thenReturn(Optional.of(agronomo));
            when(fincaRepository.findById(any(UUID.class)))
                    .thenReturn(Optional.empty());

            CrearVisitaRequest request = new CrearVisitaRequest();
            request.setFincaId(UUID.randomUUID());
            request.setFechaProgramada(LocalDateTime.now().plusDays(1));

            assertThrows(ResourceNotFoundException.class,
                    () -> service.crearVisita(request, "agronomo@test.com"));
        }
    }

    // ======================== Creación de visita ========================

    @Nested
    @DisplayName("Creación de visita")
    class CreacionVisita {

        @BeforeEach
        void setUpCreacion() {
            when(usuarioRepository.findByIdentificador("agronomo@test.com"))
                    .thenReturn(Optional.of(agronomo));
            when(fincaRepository.findById(finca.getId()))
                    .thenReturn(Optional.of(finca));
            when(agronomoFincaRepository.existsByAgronomoAndFincaAndActivaTrue(agronomo, finca))
                    .thenReturn(true);
            when(visitaRepository.countConflictos(eq(agronomo.getId()), any(), any(), any()))
                    .thenReturn(0L);
            when(visitaRepository.save(any(VisitaTecnica.class)))
                    .thenAnswer(inv -> {
                        VisitaTecnica v = inv.getArgument(0);
                        v.setId(UUID.randomUUID());
                        v.setCreatedAt(LocalDateTime.now());
                        v.setUpdatedAt(LocalDateTime.now());
                        return v;
                    });
            when(visitaImagenRepository.countByVisita(any())).thenReturn(0L);
            when(visitaImagenRepository.findByVisitaOrderByCreatedAtDesc(any()))
                    .thenReturn(java.util.Collections.emptyList());
        }

        @Test
        @DisplayName("Crea visita exitosamente con datos mínimos")
        void creaVisitaExitosamente() {
            CrearVisitaRequest request = new CrearVisitaRequest();
            request.setFincaId(finca.getId());
            request.setFechaProgramada(LocalDateTime.now().plusDays(3));
            request.setMotivo("Evaluación de moniliasis");

            VisitaDetalleResponse response = service.crearVisita(request, "agronomo@test.com");

            assertNotNull(response.getId());
            assertEquals("PROGRAMADA", response.getEstado());
            assertEquals(finca.getNombre(), response.getFincaNombre());
            assertEquals(agricultor.getNombre(), response.getAgricultorNombre());
            assertEquals("Evaluación de moniliasis", response.getMotivo());
            verify(visitaRepository).save(any(VisitaTecnica.class));
        }

        @Test
        @DisplayName("Crea visita con lote opcional")
        void creaVisitaConLote() {
            when(loteRepository.findById(lote.getId()))
                    .thenReturn(Optional.of(lote));

            CrearVisitaRequest request = new CrearVisitaRequest();
            request.setFincaId(finca.getId());
            request.setLoteId(lote.getId());
            request.setFechaProgramada(LocalDateTime.now().plusDays(3));

            VisitaDetalleResponse response = service.crearVisita(request, "agronomo@test.com");

            assertEquals(lote.getId(), response.getLoteId());
            assertEquals("Lote A", response.getLoteNombre());
        }

        @Test
        @DisplayName("Rechaza lote que no pertenece a la finca")
        void rechazaLoteDeOtraFinca() {
            Finca otraFinca = new Finca();
            otraFinca.setId(UUID.randomUUID());
            Lote loteOtraFinca = new Lote();
            loteOtraFinca.setId(UUID.randomUUID());
            loteOtraFinca.setFinca(otraFinca);

            when(loteRepository.findById(loteOtraFinca.getId()))
                    .thenReturn(Optional.of(loteOtraFinca));

            CrearVisitaRequest request = new CrearVisitaRequest();
            request.setFincaId(finca.getId());
            request.setLoteId(loteOtraFinca.getId());
            request.setFechaProgramada(LocalDateTime.now().plusDays(3));

            assertThrows(IllegalArgumentException.class,
                    () -> service.crearVisita(request, "agronomo@test.com"));
        }

        @Test
        @DisplayName("Rechaza fecha pasada")
        void rechazaFechaPasada() {
            CrearVisitaRequest request = new CrearVisitaRequest();
            request.setFincaId(finca.getId());
            request.setFechaProgramada(LocalDateTime.now().minusDays(1));

            assertThrows(IllegalArgumentException.class,
                    () -> service.crearVisita(request, "agronomo@test.com"));
        }

        @Test
        @DisplayName("Detecta conflicto de horario")
        void detectaConflictoHorario() {
            when(visitaRepository.countConflictos(eq(agronomo.getId()), any(), any(), any()))
                    .thenReturn(1L);

            CrearVisitaRequest request = new CrearVisitaRequest();
            request.setFincaId(finca.getId());
            request.setFechaProgramada(LocalDateTime.now().plusDays(3));

            assertThrows(IllegalArgumentException.class,
                    () -> service.crearVisita(request, "agronomo@test.com"));
        }
    }

    // ======================== Transiciones de estado ========================

    @Nested
    @DisplayName("Transiciones de estado")
    class TransicionesEstado {

        private VisitaTecnica visita;

        @BeforeEach
        void setUpVisita() {
            visita = new VisitaTecnica();
            visita.setId(UUID.randomUUID());
            visita.setAgronomo(agronomo);
            visita.setFinca(finca);
            visita.setAgricultor(agricultor);
            visita.setFechaProgramada(LocalDateTime.now().plusDays(1));
            visita.setEstado(EstadoVisita.PROGRAMADA);
            visita.setCreatedAt(LocalDateTime.now());
            visita.setUpdatedAt(LocalDateTime.now());

            when(usuarioRepository.findByIdentificador("agronomo@test.com"))
                    .thenReturn(Optional.of(agronomo));
            when(visitaRepository.findById(visita.getId()))
                    .thenReturn(Optional.of(visita));
            when(visitaRepository.save(any(VisitaTecnica.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            when(visitaImagenRepository.countByVisita(any())).thenReturn(0L);
            when(visitaImagenRepository.findByVisitaOrderByCreatedAtDesc(any()))
                    .thenReturn(java.util.Collections.emptyList());
        }

        @Test
        @DisplayName("PROGRAMADA → EN_CURSO al iniciar")
        void iniciarVisita() {
            VisitaDetalleResponse response = service.iniciarVisita(visita.getId(), "agronomo@test.com");

            assertEquals("EN_CURSO", response.getEstado());
            assertNotNull(response.getFechaInicioReal());
        }

        @Test
        @DisplayName("EN_CURSO → COMPLETADA al finalizar")
        void finalizarVisita() {
            visita.setEstado(EstadoVisita.EN_CURSO);
            visita.setFechaInicioReal(LocalDateTime.now().minusHours(1));

            FinalizarVisitaRequest request = new FinalizarVisitaRequest();
            request.setObservaciones("Se encontraron 3 mazorcas con moniliasis m2");
            request.setRecomendaciones("Aplicar poda sanitaria");
            request.setPlantasRevisadas(50);
            request.setPlantasEnfermas(3);
            request.setInformeFinal("Visita completada exitosamente");

            VisitaDetalleResponse response = service.finalizarVisita(
                    visita.getId(), request, "agronomo@test.com");

            assertEquals("COMPLETADA", response.getEstado());
            assertNotNull(response.getFechaFinReal());
            assertEquals("Se encontraron 3 mazorcas con moniliasis m2", response.getObservaciones());
            assertEquals(50, response.getPlantasRevisadas());
        }

        @Test
        @DisplayName("PROGRAMADA → CANCELADA con motivo")
        void cancelarVisita() {
            CancelarVisitaRequest request = new CancelarVisitaRequest();
            request.setMotivoCancelacion("Lluvia intensa impide acceso");

            VisitaDetalleResponse response = service.cancelarVisita(
                    visita.getId(), request, "agronomo@test.com");

            assertEquals("CANCELADA", response.getEstado());
            assertEquals("Lluvia intensa impide acceso", response.getMotivoCancelacion());
        }

        @Test
        @DisplayName("No se puede iniciar visita COMPLETADA")
        void noIniciarCompletada() {
            visita.setEstado(EstadoVisita.COMPLETADA);

            assertThrows(IllegalArgumentException.class,
                    () -> service.iniciarVisita(visita.getId(), "agronomo@test.com"));
        }

        @Test
        @DisplayName("No se puede finalizar visita PROGRAMADA (sin iniciar)")
        void noFinalizarSinIniciar() {
            visita.setEstado(EstadoVisita.PROGRAMADA);

            assertThrows(IllegalArgumentException.class,
                    () -> service.finalizarVisita(visita.getId(), null, "agronomo@test.com"));
        }

        @Test
        @DisplayName("No se puede cancelar visita COMPLETADA")
        void noCancelarCompletada() {
            visita.setEstado(EstadoVisita.COMPLETADA);

            CancelarVisitaRequest request = new CancelarVisitaRequest();
            request.setMotivoCancelacion("Intento inválido");

            assertThrows(IllegalArgumentException.class,
                    () -> service.cancelarVisita(visita.getId(), request, "agronomo@test.com"));
        }

        @Test
        @DisplayName("No se puede reprogramar visita EN_CURSO")
        void noReprogramarEnCurso() {
            visita.setEstado(EstadoVisita.EN_CURSO);

            ReprogramarVisitaRequest request = new ReprogramarVisitaRequest();
            request.setFechaProgramada(LocalDateTime.now().plusDays(5));

            assertThrows(IllegalArgumentException.class,
                    () -> service.reprogramarVisita(visita.getId(), request, "agronomo@test.com"));
        }
    }

    // ======================== Acceso cruzado ========================

    @Nested
    @DisplayName("Acceso cruzado entre agrónomos")
    class AccesoCruzado {

        @Test
        @DisplayName("Agrónomo no puede acceder a visita de otro agrónomo")
        void otroAgronomoNoAccede() {
            Usuario otroAgronomo = new Usuario();
            otroAgronomo.setId(UUID.randomUUID());
            otroAgronomo.setRol(Rol.EXPERTO);

            VisitaTecnica visita = new VisitaTecnica();
            visita.setId(UUID.randomUUID());
            visita.setAgronomo(agronomo); // Pertenece al primer agrónomo
            visita.setFinca(finca);
            visita.setAgricultor(agricultor);

            when(usuarioRepository.findByIdentificador("otro@test.com"))
                    .thenReturn(Optional.of(otroAgronomo));
            when(visitaRepository.findById(visita.getId()))
                    .thenReturn(Optional.of(visita));

            assertThrows(IllegalArgumentException.class,
                    () -> service.obtenerDetalle(visita.getId(), "otro@test.com"));
        }

        @Test
        @DisplayName("Agrónomo no puede iniciar visita de otro agrónomo")
        void otroAgronomoNoInicia() {
            Usuario otroAgronomo = new Usuario();
            otroAgronomo.setId(UUID.randomUUID());
            otroAgronomo.setRol(Rol.EXPERTO);

            VisitaTecnica visita = new VisitaTecnica();
            visita.setId(UUID.randomUUID());
            visita.setAgronomo(agronomo);
            visita.setEstado(EstadoVisita.PROGRAMADA);

            when(usuarioRepository.findByIdentificador("otro@test.com"))
                    .thenReturn(Optional.of(otroAgronomo));
            when(visitaRepository.findById(visita.getId()))
                    .thenReturn(Optional.of(visita));

            assertThrows(IllegalArgumentException.class,
                    () -> service.iniciarVisita(visita.getId(), "otro@test.com"));
        }
    }
}
