package com.cacaoscan.backend.repository;

import com.cacaoscan.backend.model.VisitaImagen;
import com.cacaoscan.backend.model.VisitaTecnica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface VisitaImagenRepository extends JpaRepository<VisitaImagen, UUID> {

    List<VisitaImagen> findByVisitaOrderByCreatedAtDesc(VisitaTecnica visita);

    long countByVisita(VisitaTecnica visita);
}
