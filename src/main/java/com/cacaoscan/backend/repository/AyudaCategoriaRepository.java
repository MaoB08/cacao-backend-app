package com.cacaoscan.backend.repository;

import com.cacaoscan.backend.model.AyudaCategoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AyudaCategoriaRepository extends JpaRepository<AyudaCategoria, UUID> {
    List<AyudaCategoria> findByActivoTrueOrderByOrdenAsc();
}
