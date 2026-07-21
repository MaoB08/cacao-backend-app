package com.cacaoscan.backend.repository;

import com.cacaoscan.backend.model.AyudaPregunta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AyudaPreguntaRepository extends JpaRepository<AyudaPregunta, UUID> {
    List<AyudaPregunta> findByActivoTrueOrderByOrdenAsc();
}
