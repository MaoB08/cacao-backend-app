package com.cacaoscan.backend.repository;

import com.cacaoscan.backend.model.Inspeccion;
import com.cacaoscan.backend.model.Lote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InspeccionRepository extends JpaRepository<Inspeccion, UUID> {

    List<Inspeccion> findByLoteOrderByFechaInspeccionDesc(Lote lote);

    @Query("SELECT COUNT(i) FROM Inspeccion i WHERE i.lote.id = :loteId")
    long countByLoteId(@Param("loteId") UUID loteId);

    @Query("SELECT i FROM Inspeccion i WHERE i.lote.id = :loteId ORDER BY i.fechaInspeccion DESC LIMIT 1")
    Inspeccion findUltimaInspeccionByLoteId(@Param("loteId") UUID loteId);

    @Query("SELECT i FROM Inspeccion i WHERE i.usuario.id = :usuarioId ORDER BY i.fechaInspeccion DESC")
    List<Inspeccion> findAllByUsuarioId(@Param("usuarioId") UUID usuarioId);
}
