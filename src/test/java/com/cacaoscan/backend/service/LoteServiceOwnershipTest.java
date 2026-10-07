package com.cacaoscan.backend.service;

import com.cacaoscan.backend.model.Finca;
import com.cacaoscan.backend.model.Lote;
import com.cacaoscan.backend.model.Usuario;
import com.cacaoscan.backend.repository.*;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LoteServiceOwnershipTest {

    @Test
    void rejectsReadingAnotherUsersLote() {
        LoteRepository loteRepository = mock(LoteRepository.class);
        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        LoteService service = new LoteService(
                loteRepository,
                mock(FincaRepository.class),
                usuarioRepository,
                mock(InspeccionRepository.class),
                mock(DeteccionGeoreferenciadaRepository.class));

        Usuario authenticated = new Usuario();
        authenticated.setId(UUID.randomUUID());
        Usuario owner = new Usuario();
        owner.setId(UUID.randomUUID());
        Finca finca = new Finca();
        finca.setUsuario(owner);
        Lote lote = new Lote();
        lote.setFinca(finca);
        UUID loteId = UUID.randomUUID();

        when(loteRepository.findById(loteId)).thenReturn(Optional.of(lote));
        when(usuarioRepository.findByIdentificador("attacker@example.com")).thenReturn(Optional.of(authenticated));

        assertThrows(IllegalArgumentException.class,
                () -> service.obtenerDetalle(loteId, "attacker@example.com"));
    }
}
