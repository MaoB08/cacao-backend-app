package com.cacaoscan.backend.repository;

import com.cacaoscan.backend.model.Diagnostico;
import com.cacaoscan.backend.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DiagnosticoRepository extends JpaRepository<Diagnostico, UUID> {
    List<Diagnostico> findByUsuarioOrderByCreatedAtDesc(Usuario usuario);
    List<Diagnostico> findTop5ByUsuarioOrderByCreatedAtDesc(Usuario usuario);
}
