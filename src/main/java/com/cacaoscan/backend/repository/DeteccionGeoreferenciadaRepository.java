package com.cacaoscan.backend.repository;

import com.cacaoscan.backend.model.DeteccionGeoreferenciada;
import com.cacaoscan.backend.model.Lote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DeteccionGeoreferenciadaRepository extends JpaRepository<DeteccionGeoreferenciada, UUID> {

    List<DeteccionGeoreferenciada> findByLoteOrderByFechaDeteccionDesc(Lote lote);

    @Query("SELECT d FROM DeteccionGeoreferenciada d WHERE d.inspeccion.id = :inspeccionId ORDER BY d.fechaDeteccion DESC")
    List<DeteccionGeoreferenciada> findByInspeccionId(@Param("inspeccionId") UUID inspeccionId);

    @Query("SELECT d FROM DeteccionGeoreferenciada d WHERE d.usuario.id = :usuarioId ORDER BY d.fechaDeteccion DESC")
    List<DeteccionGeoreferenciada> findAllByUsuarioId(@Param("usuarioId") UUID usuarioId);

    /**
     * Conteo de detecciones agrupado por enfermedad para un lote específico.
     * Se usa para el resumen del Bottom Sheet: "Monilia: 12, Escoba de Bruja: 6..."
     */
    @Query("SELECT d.enfermedad, COUNT(d) FROM DeteccionGeoreferenciada d WHERE d.lote.id = :loteId GROUP BY d.enfermedad ORDER BY COUNT(d) DESC")
    List<Object[]> contarPorEnfermedadYLote(@Param("loteId") UUID loteId);

    @Query("SELECT COUNT(d) FROM DeteccionGeoreferenciada d WHERE d.lote.id = :loteId")
    long countByLoteId(@Param("loteId") UUID loteId);
}
