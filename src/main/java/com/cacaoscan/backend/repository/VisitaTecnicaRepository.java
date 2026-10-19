package com.cacaoscan.backend.repository;

import com.cacaoscan.backend.model.EstadoVisita;
import com.cacaoscan.backend.model.Usuario;
import com.cacaoscan.backend.model.VisitaTecnica;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface VisitaTecnicaRepository extends JpaRepository<VisitaTecnica, UUID> {

    /**
     * Lista visitas del agrónomo con filtros opcionales.
     */
    @Query(value = "SELECT v FROM VisitaTecnica v " +
           "JOIN FETCH v.finca f " +
           "JOIN FETCH v.agricultor a " +
           "LEFT JOIN FETCH v.lote " +
           "WHERE v.agronomo = :agronomo " +
           "AND (cast(:estado as text) IS NULL OR v.estado = :estado) " +
           "AND (cast(:fincaId as uuid) IS NULL OR f.id = :fincaId) " +
           "AND (cast(:desde as timestamp) IS NULL OR v.fechaProgramada >= :desde) " +
           "AND (cast(:hasta as timestamp) IS NULL OR v.fechaProgramada <= :hasta) " +
           "ORDER BY v.fechaProgramada ASC",
           countQuery = "SELECT COUNT(v) FROM VisitaTecnica v " +
           "WHERE v.agronomo = :agronomo " +
           "AND (cast(:estado as text) IS NULL OR v.estado = :estado) " +
           "AND (cast(:fincaId as uuid) IS NULL OR v.finca.id = :fincaId) " +
           "AND (cast(:desde as timestamp) IS NULL OR v.fechaProgramada >= :desde) " +
           "AND (cast(:hasta as timestamp) IS NULL OR v.fechaProgramada <= :hasta)")
    Page<VisitaTecnica> findByAgronomoWithFilters(
            @Param("agronomo") Usuario agronomo,
            @Param("estado") EstadoVisita estado,
            @Param("fincaId") UUID fincaId,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta,
            Pageable pageable);

    /**
     * Próximas visitas programadas/en curso del agrónomo (para dashboard).
     */
    @Query("SELECT v FROM VisitaTecnica v " +
           "JOIN FETCH v.finca f " +
           "JOIN FETCH v.agricultor " +
           "LEFT JOIN FETCH v.lote " +
           "WHERE v.agronomo = :agronomo " +
           "AND v.estado IN (com.cacaoscan.backend.model.EstadoVisita.PROGRAMADA, com.cacaoscan.backend.model.EstadoVisita.EN_CURSO) " +
           "AND v.fechaProgramada >= :desde " +
           "ORDER BY v.fechaProgramada ASC")
    List<VisitaTecnica> findProximasVisitas(
            @Param("agronomo") Usuario agronomo,
            @Param("desde") LocalDateTime desde,
            Pageable pageable);

    /**
     * Detectar conflictos de horario del agrónomo mediante query nativa de PostgreSQL.
     * Busca visitas del mismo agrónomo que se solapen con el rango dado.
     */
    @Query(value = "SELECT COUNT(*) FROM visitas_tecnicas v " +
           "WHERE v.agronomo_id = :agronomoId " +
           "AND v.estado IN ('PROGRAMADA', 'EN_CURSO') " +
           "AND v.id <> :excludeId " +
           "AND v.fecha_programada < :fin " +
           "AND (v.fecha_programada + (COALESCE(v.duracion_estimada_min, 60) * INTERVAL '1 minute') > :inicio)",
           nativeQuery = true)
    long countConflictos(
            @Param("agronomoId") UUID agronomoId,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin,
            @Param("excludeId") UUID excludeId);

    /**
     * Contadores para dashboard del agrónomo.
     */
    long countByAgronomoAndEstado(Usuario agronomo, EstadoVisita estado);

    @Query("SELECT COUNT(v) FROM VisitaTecnica v " +
           "WHERE v.agronomo = :agronomo " +
           "AND v.estado = com.cacaoscan.backend.model.EstadoVisita.COMPLETADA " +
           "AND v.fechaFinReal >= :desde")
    long countCompletadasDesde(
            @Param("agronomo") Usuario agronomo,
            @Param("desde") LocalDateTime desde);

    // ======================== Consultas para el agricultor (MA-19) ========================

    /**
     * Lista visitas asociadas a las fincas del agricultor, con filtro opcional por finca.
     * Excluye notas internas del agrónomo (se filtra en el mapper, no en la query).
     */
    @Query(value = "SELECT v FROM VisitaTecnica v " +
           "JOIN FETCH v.finca f " +
           "JOIN FETCH v.agronomo ag " +
           "LEFT JOIN FETCH v.lote " +
           "WHERE v.agricultor = :agricultor " +
           "AND (cast(:fincaId as uuid) IS NULL OR f.id = :fincaId) " +
           "ORDER BY v.fechaProgramada DESC",
           countQuery = "SELECT COUNT(v) FROM VisitaTecnica v " +
           "WHERE v.agricultor = :agricultor " +
           "AND (cast(:fincaId as uuid) IS NULL OR v.finca.id = :fincaId)")
    Page<VisitaTecnica> findByAgricultorWithFilters(
            @Param("agricultor") Usuario agricultor,
            @Param("fincaId") UUID fincaId,
            Pageable pageable);

    /**
     * Cuenta las visitas próximas (PROGRAMADA o EN_CURSO) para las fincas del agricultor.
     */
    @Query("SELECT COUNT(v) FROM VisitaTecnica v " +
           "WHERE v.agricultor = :agricultor " +
           "AND v.estado IN (com.cacaoscan.backend.model.EstadoVisita.PROGRAMADA, " +
           "com.cacaoscan.backend.model.EstadoVisita.EN_CURSO)")
    long countProximasVisitasAgricultor(@Param("agricultor") Usuario agricultor);

    /**
     * Próximas visitas del agricultor para su dashboard (max N).
     */
    @Query("SELECT v FROM VisitaTecnica v " +
           "JOIN FETCH v.finca f " +
           "JOIN FETCH v.agronomo ag " +
           "LEFT JOIN FETCH v.lote " +
           "WHERE v.agricultor = :agricultor " +
           "AND v.estado IN (com.cacaoscan.backend.model.EstadoVisita.PROGRAMADA, " +
           "com.cacaoscan.backend.model.EstadoVisita.EN_CURSO) " +
           "AND v.fechaProgramada >= :desde " +
           "ORDER BY v.fechaProgramada ASC")
    List<VisitaTecnica> findProximasVisitasAgricultor(
            @Param("agricultor") Usuario agricultor,
            @Param("desde") LocalDateTime desde,
            Pageable pageable);
}
