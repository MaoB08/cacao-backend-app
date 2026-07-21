package com.cacaoscan.backend.repository;

import com.cacaoscan.backend.model.Experto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ExpertoRepository extends JpaRepository<Experto, UUID> {

    List<Experto> findByActivoTrueOrderByNombreAsc();

    List<Experto> findByActivoTrueAndEspecialidadIgnoreCaseOrderByNombreAsc(String especialidad);

    @Query("SELECT e FROM Experto e WHERE e.activo = true AND " +
           "(LOWER(e.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
           "LOWER(e.especialidad) LIKE LOWER(CONCAT('%', :busqueda, '%')))")
    List<Experto> buscarPorNombreOEspecialidad(@Param("busqueda") String busqueda);

    @Query("SELECT e FROM Experto e WHERE e.activo = true AND " +
           "e.especialidad = :especialidad AND " +
           "(LOWER(e.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
           "LOWER(e.especialidad) LIKE LOWER(CONCAT('%', :busqueda, '%')))")
    List<Experto> buscarPorEspecialidadYTexto(@Param("especialidad") String especialidad,
                                              @Param("busqueda") String busqueda);

    @Query("SELECT DISTINCT e.especialidad FROM Experto e WHERE e.activo = true ORDER BY e.especialidad")
    List<String> findDistinctEspecialidades();
}
