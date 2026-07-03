package com.cacaoscan.backend.repository;

import com.cacaoscan.backend.model.RefreshToken;
import com.cacaoscan.backend.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.usuario = :usuario")
    void deleteByUsuario(Usuario usuario);

    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.usuario.id = :usuarioId")
    void deleteByUsuarioId(UUID usuarioId);
}
