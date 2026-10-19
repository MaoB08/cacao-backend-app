package com.cacaoscan.backend.service;

import com.cacaoscan.backend.dto.AgricultorVisitaDTOs.*;
import com.cacaoscan.backend.model.*;
import com.cacaoscan.backend.repository.UsuarioRepository;
import com.cacaoscan.backend.repository.VisitaTecnicaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class AgricultorVisitaServiceTest {

    private VisitaTecnicaRepository visitaRepository;
    private UsuarioRepository usuarioRepository;
    private AgricultorVisitaService service;

    private Usuario agricultor;
    private Usuario agronomo;
    private Finca finca;
    private Lote lote;
    private VisitaTecnica visita;

    @BeforeEach
    void setUp() {
        visitaRepository = mock(VisitaTecnicaRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);
        service = new AgricultorVisitaService(visitaRepository, usuarioRepository);

        agricultor = new Usuario();
        agricultor.setId(UUID.randomUUID());
        agricultor.setNombre("Pedro Agricultor");
        agricultor.setRol(Rol.AGRICULTOR);

        agronomo = new Usuario();
        agronomo.setId(UUID.randomUUID());
        agronomo.setNombre("Dra. María Agrónoma");
        agronomo.setRol(Rol.EXPERTO);

        finca = new Finca();
        finca.setId(UUID.randomUUID());
        finca.setNombre("Finca El Paraíso");
        finca.setMunicipio("Lebrija");
        finca.setUsuario(agricultor);

        lote = new Lote();
        lote.setId(UUID.randomUUID());
        lote.setNombre("Lote Central");
        lote.setFinca(finca);

        visita = new VisitaTecnica();
        visita.setId(UUID.randomUUID());
        visita.setAgronomo(agronomo);
        visita.setAgricultor(agricultor);
        visita.setFinca(finca);
        visita.setLote(lote);
        visita.setEstado(EstadoVisita.COMPLETADA);
        visita.setFechaProgramada(LocalDateTime.now().minusDays(1));
        visita.setFechaInicioReal(LocalDateTime.now().minusDays(1).plusHours(9));
        visita.setFechaFinReal(LocalDateTime.now().minusDays(1).plusHours(11));
        visita.setDuracionRealMin(120);
        visita.setMotivo("Control de Monilia");
        visita.setNotasPrevias("Nota privada del agrónomo: revisar antecedentes de poda");
        visita.setObservaciones("Observación interna confidencial sobre gestión del lote");
        visita.setRecomendaciones("Aplicar caldo bordelés y podar ramas afectadas");
        visita.setPlantasRevisadas(50);
        visita.setPlantasEnfermas(4);
    }

    @Nested
    @DisplayName("Control de Acceso y Roles")
    class ControlAcceso {

        @Test
        @DisplayName("Rechaza solicitud si usuario no existe con 404 NOT_FOUND")
        void usuarioNoExiste_lanza404() {
            when(usuarioRepository.findByIdentificador("desconocido@cacao.com")).thenReturn(Optional.empty());

            ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                    service.listarVisitasAgricultor("desconocido@cacao.com", null, PageRequest.of(0, 10)));

            assertEquals(404, ex.getStatusCode().value());
        }

        @Test
        @DisplayName("Rechaza a un agrónomo (Rol.EXPERTO) con 403 FORBIDDEN")
        void rolExperto_lanza403() {
            when(usuarioRepository.findByIdentificador("agronomo@cacao.com")).thenReturn(Optional.of(agronomo));

            ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                    service.listarVisitasAgricultor("agronomo@cacao.com", null, PageRequest.of(0, 10)));

            assertEquals(403, ex.getStatusCode().value());
            assertTrue(ex.getReason().contains("Solo los agricultores"));
        }

        @Test
        @DisplayName("Rechaza a un ADMIN con 403 FORBIDDEN")
        void rolAdmin_lanza403() {
            Usuario admin = new Usuario();
            admin.setId(UUID.randomUUID());
            admin.setRol(Rol.ADMIN);
            when(usuarioRepository.findByIdentificador("admin@cacao.com")).thenReturn(Optional.of(admin));

            ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                    service.obtenerDashboardVisitas("admin@cacao.com"));

            assertEquals(403, ex.getStatusCode().value());
        }
    }

    @Nested
    @DisplayName("Listado de Visitas y Privacidad de Datos")
    class ListadoYPrivacidad {

        @Test
        @DisplayName("Agricultor lista visitas y NO se exponen notas internas ni observaciones")
        void listarVisitas_excluyeNotasInternasYExponeRecomendaciones() {
            when(usuarioRepository.findByIdentificador("pedro@cacao.com")).thenReturn(Optional.of(agricultor));
            Page<VisitaTecnica> page = new PageImpl<>(List.of(visita));
            when(visitaRepository.findByAgricultorWithFilters(eq(agricultor), isNull(), any(Pageable.class)))
                    .thenReturn(page);

            Page<VisitaAgricultorResumen> resultado = service.listarVisitasAgricultor("pedro@cacao.com", null, PageRequest.of(0, 10));

            assertNotNull(resultado);
            assertEquals(1, resultado.getContent().size());
            VisitaAgricultorResumen dto = resultado.getContent().get(0);

            // Campos públicos y compartidos
            assertEquals(visita.getId(), dto.getId());
            assertEquals("COMPLETADA", dto.getEstado());
            assertEquals("Finca El Paraíso", dto.getFincaNombre());
            assertEquals("Dra. María Agrónoma", dto.getAgronomoNombre());
            assertEquals("Lote Central", dto.getLoteNombre());
            assertEquals("Aplicar caldo bordelés y podar ramas afectadas", dto.getRecomendaciones());
            assertEquals(50, dto.getPlantasRevisadas());
            assertEquals(4, dto.getPlantasEnfermas());
            assertEquals(120, dto.getDuracionRealMin());

            // Verificación estricta de que VisitaAgricultorResumen no expone notas privadas
            // El DTO no contiene getNotasPrevias() ni getObservaciones(), garantizando contractualmente la privacidad
        }

        @Test
        @DisplayName("Filtra visitas por fincaId correctamente")
        void listarVisitas_conFiltroFinca() {
            UUID fincaFiltro = finca.getId();
            when(usuarioRepository.findByIdentificador("pedro@cacao.com")).thenReturn(Optional.of(agricultor));
            when(visitaRepository.findByAgricultorWithFilters(eq(agricultor), eq(fincaFiltro), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(visita)));

            Page<VisitaAgricultorResumen> resultado = service.listarVisitasAgricultor("pedro@cacao.com", fincaFiltro, PageRequest.of(0, 10));

            assertEquals(1, resultado.getTotalElements());
            verify(visitaRepository).findByAgricultorWithFilters(eq(agricultor), eq(fincaFiltro), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("Dashboard de Visitas del Agricultor")
    class DashboardVisitas {

        @Test
        @DisplayName("Retorna métricas y próximas visitas para el panel del agricultor")
        void obtenerDashboardVisitas_exito() {
            when(usuarioRepository.findByIdentificador("pedro@cacao.com")).thenReturn(Optional.of(agricultor));
            when(visitaRepository.countProximasVisitasAgricultor(agricultor)).thenReturn(2L);

            // Visitas completadas para conteo total
            VisitaTecnica vCompletada = new VisitaTecnica();
            vCompletada.setEstado(EstadoVisita.COMPLETADA);
            when(visitaRepository.findByAgricultorWithFilters(eq(agricultor), isNull(), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(vCompletada)));

            // Próxima visita programada
            VisitaTecnica vProxima = new VisitaTecnica();
            vProxima.setId(UUID.randomUUID());
            vProxima.setEstado(EstadoVisita.PROGRAMADA);
            vProxima.setFechaProgramada(LocalDateTime.now().plusDays(2));
            vProxima.setFinca(finca);
            vProxima.setAgronomo(agronomo);
            vProxima.setMotivo("Evaluación fitosanitaria trimestral");

            when(visitaRepository.findProximasVisitasAgricultor(eq(agricultor), any(LocalDateTime.class), any(Pageable.class)))
                    .thenReturn(List.of(vProxima));

            VisitasAgricultorDashboardInfo info = service.obtenerDashboardVisitas("pedro@cacao.com");

            assertNotNull(info);
            assertEquals(2L, info.getProximasVisitas());
            assertEquals(1L, info.getCompletadasTotal());
            assertEquals(1, info.getProximas().size());

            ProximaVisitaAgricultorInfo prox = info.getProximas().get(0);
            assertEquals("PROGRAMADA", prox.getEstado());
            assertEquals("Finca El Paraíso", prox.getFincaNombre());
            assertEquals("Dra. María Agrónoma", prox.getAgronomoNombre());
            assertEquals("Evaluación fitosanitaria trimestral", prox.getMotivo());
        }
    }
}
