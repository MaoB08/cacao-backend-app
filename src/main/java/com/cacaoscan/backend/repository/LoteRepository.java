package com.cacaoscan.backend.repository;

import com.cacaoscan.backend.model.Finca;
import com.cacaoscan.backend.model.Lote;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LoteRepository extends JpaRepository<Lote, UUID> {

    List<Lote> findByFincaOrderByCreatedAtDesc(Finca finca);

    /**
     * Obtiene todos los lotes de todas las fincas de un usuario.
     * Utilizado para renderizar todos los polígonos en la vista de mapa.
     */
    @Query("SELECT l FROM Lote l JOIN l.finca f WHERE f.usuario.id = :usuarioId ORDER BY l.createdAt DESC")
    List<Lote> findAllByUsuarioId(@Param("usuarioId") UUID usuarioId);

    /**
     * Busca el lote que contiene geográficamente un punto dado.
     * Utiliza la función ST_Contains de PostGIS para determinar
     * a qué lote pertenece una coordenada de detección de enfermedad.
     */
    @Query(value = "SELECT l.* FROM lotes l WHERE ST_Contains(l.geometria, :punto)", nativeQuery = true)
    Optional<Lote> findByContainingPoint(@Param("punto") Point punto);

    /**
     * Calcula el área del polígono en hectáreas usando funciones geodésicas de PostGIS.
     * ST_Area(geometry::geography) devuelve el área en metros cuadrados sobre el elipsoide WGS84.
     */
    @Query(value = "SELECT ST_Area(l.geometria::geography) / 10000.0 FROM lotes l WHERE l.id = :loteId", nativeQuery = true)
    Double calcularAreaHectareas(@Param("loteId") UUID loteId);

    /**
     * Calcula el perímetro del polígono en metros.
     */
    @Query(value = "SELECT ST_Perimeter(l.geometria::geography) FROM lotes l WHERE l.id = :loteId", nativeQuery = true)
    Double calcularPerimetroMetros(@Param("loteId") UUID loteId);

    boolean existsByFincaAndNombreIgnoreCase(Finca finca, String nombre);
}
