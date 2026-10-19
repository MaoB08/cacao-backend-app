package com.cacaoscan.backend.repository;

import com.cacaoscan.backend.model.AgronomoFinca;
import com.cacaoscan.backend.model.Finca;
import com.cacaoscan.backend.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AgronomoFincaRepository extends JpaRepository<AgronomoFinca, UUID> {

    /**
     * Verifica si existe una asignación activa entre agrónomo y finca.
     */
    boolean existsByAgronomoAndFincaAndActivaTrue(Usuario agronomo, Finca finca);

    /**
     * Lista las fincas activas asignadas al agrónomo.
     */
    @Query(value = "SELECT af FROM AgronomoFinca af " +
           "JOIN FETCH af.finca f " +
           "JOIN FETCH f.usuario " +
           "WHERE af.agronomo = :agronomo " +
           "AND af.activa = true " +
           "AND f.activa = true " +
           "ORDER BY f.nombre ASC",
           countQuery = "SELECT COUNT(af) FROM AgronomoFinca af " +
           "WHERE af.agronomo = :agronomo " +
           "AND af.activa = true " +
           "AND af.finca.activa = true")
    Page<AgronomoFinca> findFincasAsignadas(
            @Param("agronomo") Usuario agronomo,
            Pageable pageable);

    /**
     * Lista las fincas activas asignadas al agrónomo filtradas por nombre o municipio.
     */
    @Query(value = "SELECT af FROM AgronomoFinca af " +
           "JOIN FETCH af.finca f " +
           "JOIN FETCH f.usuario " +
           "WHERE af.agronomo = :agronomo " +
           "AND af.activa = true " +
           "AND f.activa = true " +
           "AND (LOWER(f.nombre) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "     OR LOWER(f.municipio) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY f.nombre ASC",
           countQuery = "SELECT COUNT(af) FROM AgronomoFinca af " +
           "JOIN af.finca f " +
           "WHERE af.agronomo = :agronomo " +
           "AND af.activa = true " +
           "AND af.finca.activa = true " +
           "AND (LOWER(f.nombre) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "     OR LOWER(f.municipio) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<AgronomoFinca> findFincasAsignadasConBusqueda(
            @Param("agronomo") Usuario agronomo,
            @Param("search") String search,
            Pageable pageable);

    /**
     * Cuenta fincas activas asignadas al agrónomo.
     */
    @Query("SELECT COUNT(af) FROM AgronomoFinca af " +
           "WHERE af.agronomo = :agronomo AND af.activa = true AND af.finca.activa = true")
    long countFincasActivasByAgronomo(@Param("agronomo") Usuario agronomo);
}
