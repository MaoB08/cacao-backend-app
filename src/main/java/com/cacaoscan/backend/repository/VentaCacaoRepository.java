package com.cacaoscan.backend.repository;

import com.cacaoscan.backend.model.VentaCacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VentaCacaoRepository extends JpaRepository<VentaCacao, UUID> {

    List<VentaCacao> findByUsuarioIdOrderByFechaDesc(UUID usuarioId);

    Optional<VentaCacao> findByIdAndUsuarioId(UUID id, UUID usuarioId);

    void deleteByUsuarioId(UUID usuarioId);
}
