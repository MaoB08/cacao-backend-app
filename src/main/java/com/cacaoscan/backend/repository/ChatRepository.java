package com.cacaoscan.backend.repository;

import com.cacaoscan.backend.model.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChatRepository extends JpaRepository<Chat, UUID> {
    List<Chat> findByAgricultorIdOrderByCreatedAtDesc(UUID agricultorId);

    @Query("SELECT COUNT(DISTINCT c.agricultor.id) FROM Chat c WHERE LOWER(c.experto.nombre) = LOWER(:expertoNombre)")
    long countUniqueAgricultoresByExpertoNombre(@Param("expertoNombre") String expertoNombre);

    @Query("SELECT COUNT(DISTINCT c.agricultor.id) FROM Chat c")
    long countTotalUniqueAgricultores();
}
