package com.cacaoscan.backend.repository;

import com.cacaoscan.backend.model.Finca;
import com.cacaoscan.backend.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FincaRepository extends JpaRepository<Finca, UUID> {

    List<Finca> findByUsuarioAndActivaTrueOrderByCreatedAtDesc(Usuario usuario);

    List<Finca> findByUsuarioOrderByCreatedAtDesc(Usuario usuario);

    boolean existsByUsuarioAndNombreIgnoreCase(Usuario usuario, String nombre);
}
