package com.cacaoscan.backend.service;

import com.cacaoscan.backend.dto.ChatIniciarResponse;
import com.cacaoscan.backend.dto.ExpertoResponse;
import com.cacaoscan.backend.model.Chat;
import com.cacaoscan.backend.model.Experto;
import com.cacaoscan.backend.model.Usuario;
import com.cacaoscan.backend.repository.ChatRepository;
import com.cacaoscan.backend.repository.ExpertoRepository;
import com.cacaoscan.backend.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ExpertoService {

    private final ExpertoRepository expertoRepository;
    private final ChatRepository chatRepository;
    private final UsuarioRepository usuarioRepository;

    public ExpertoService(ExpertoRepository expertoRepository,
                          ChatRepository chatRepository,
                          UsuarioRepository usuarioRepository) {
        this.expertoRepository = expertoRepository;
        this.chatRepository = chatRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Lista expertos con filtros opcionales de especialidad y búsqueda.
     */
    public List<ExpertoResponse> listarExpertos(String especialidad, String busqueda) {
        List<Experto> expertos;

        boolean tieneEspecialidad = especialidad != null && !especialidad.isBlank() && !"Todos".equalsIgnoreCase(especialidad);
        boolean tieneBusqueda = busqueda != null && !busqueda.isBlank();

        if (tieneEspecialidad && tieneBusqueda) {
            expertos = expertoRepository.buscarPorEspecialidadYTexto(especialidad, busqueda);
        } else if (tieneEspecialidad) {
            expertos = expertoRepository.findByActivoTrueAndEspecialidadIgnoreCaseOrderByNombreAsc(especialidad);
        } else if (tieneBusqueda) {
            expertos = expertoRepository.buscarPorNombreOEspecialidad(busqueda);
        } else {
            expertos = expertoRepository.findByActivoTrueOrderByNombreAsc();
        }

        return expertos.stream().map(this::toResponse).collect(Collectors.toList());
    }

    /**
     * Devuelve la lista de especialidades únicas con "Todos" al inicio.
     */
    public List<String> listarEspecialidades() {
        List<String> especialidades = new ArrayList<>();
        especialidades.add("Todos");
        especialidades.addAll(expertoRepository.findDistinctEspecialidades());
        return especialidades;
    }

    /**
     * Inicia un chat entre un agricultor y un experto.
     */
    @Transactional
    public ChatIniciarResponse iniciarChat(String email, UUID expertoId) {
        Usuario agricultor = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("usuario_no_encontrado"));

        Experto experto = expertoRepository.findById(expertoId)
                .orElseThrow(() -> new IllegalArgumentException("experto_no_encontrado"));

        if (!experto.isDisponible()) {
            throw new ExpertoNoDisponibleException(
                    experto.getTiempoEsperaMinutos() != null ? experto.getTiempoEsperaMinutos() : 0
            );
        }

        Chat chat = new Chat();
        chat.setAgricultor(agricultor);
        chat.setExperto(experto);
        chat.setEstado("ACTIVO");
        chat = chatRepository.save(chat);

        return new ChatIniciarResponse(chat.getId(), "Chat iniciado");
    }

    private ExpertoResponse toResponse(Experto experto) {
        ExpertoResponse response = new ExpertoResponse();
        response.setId(experto.getId());
        response.setNombre(experto.getNombre());
        response.setEspecialidad(experto.getEspecialidad());
        response.setDescripcion(experto.getDescripcion());
        response.setRating(experto.getRating());
        response.setFotoPerfil(experto.getFotoPerfilUrl());
        response.setDisponible(experto.isDisponible());
        response.setTiempoEspera(experto.isDisponible() ? null : experto.getTiempoEsperaMinutos());
        return response;
    }

    /**
     * Excepción específica para experto no disponible (HTTP 423).
     */
    public static class ExpertoNoDisponibleException extends RuntimeException {
        private final int tiempoEspera;

        public ExpertoNoDisponibleException(int tiempoEspera) {
            super("experto_no_disponible");
            this.tiempoEspera = tiempoEspera;
        }

        public int getTiempoEspera() { return tiempoEspera; }
    }
}
