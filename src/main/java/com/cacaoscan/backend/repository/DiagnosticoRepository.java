package com.cacaoscan.backend.repository;

import com.cacaoscan.backend.model.Diagnostico;
import com.cacaoscan.backend.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DiagnosticoRepository extends JpaRepository<Diagnostico, UUID> {
    List<Diagnostico> findByUsuarioOrderByCreatedAtDesc(Usuario usuario);
    List<Diagnostico> findTop5ByUsuarioOrderByCreatedAtDesc(Usuario usuario);

    @Query("SELECT COUNT(d) FROM Diagnostico d WHERE d.usuario.id = :usuarioId")
    long countByUsuarioId(@Param("usuarioId") UUID usuarioId);

    @Query("SELECT COUNT(d) FROM Diagnostico d WHERE d.usuario.id = :usuarioId AND d.tieneEnfermedad = true AND (d.severidad LIKE '%Alta%' OR d.severidad LIKE '%ALTA%')")
    long countAlertasActivasByUsuarioId(@Param("usuarioId") UUID usuarioId);

    java.util.Optional<Diagnostico> findFirstByUsuarioOrderByCreatedAtDesc(Usuario usuario);

    List<Diagnostico> findTop10ByTieneEnfermedadTrueOrderByCreatedAtDesc();

    long countByTieneEnfermedadTrue();

    @Query("SELECT COUNT(d) FROM Diagnostico d WHERE d.tieneEnfermedad = true AND (d.severidad LIKE '%Alta%' OR d.severidad LIKE '%ALTA%')")
    long countAlertasTotales();
}
