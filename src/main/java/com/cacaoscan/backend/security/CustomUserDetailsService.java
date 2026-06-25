package com.cacaoscan.backend.security;

import com.cacaoscan.backend.model.Usuario;
import com.cacaoscan.backend.repository.UsuarioRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String usernameOrIdentifier) throws UsernameNotFoundException {
        String identifier = usernameOrIdentifier.toLowerCase().trim();
        Usuario usuario = usuarioRepository.findByEmailOrTelefono(identifier, identifier)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con el correo o teléfono: " + usernameOrIdentifier));

        if (!usuario.isActivo()) {
            throw new RuntimeException("El usuario está deshabilitado");
        }

        return new User(
                usuario.getEmail(),
                usuario.getPasswordHash(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name()))
        );
    }
}
