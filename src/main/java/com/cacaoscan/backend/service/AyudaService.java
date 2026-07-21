package com.cacaoscan.backend.service;

import com.cacaoscan.backend.dto.AyudaCategoriaResponse;
import com.cacaoscan.backend.dto.AyudaPreguntaResponse;
import com.cacaoscan.backend.model.AyudaCategoria;
import com.cacaoscan.backend.model.AyudaPregunta;
import com.cacaoscan.backend.repository.AyudaCategoriaRepository;
import com.cacaoscan.backend.repository.AyudaPreguntaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AyudaService {

    private final AyudaPreguntaRepository preguntaRepository;
    private final AyudaCategoriaRepository categoriaRepository;

    public AyudaService(AyudaPreguntaRepository preguntaRepository,
                        AyudaCategoriaRepository categoriaRepository) {
        this.preguntaRepository = preguntaRepository;
        this.categoriaRepository = categoriaRepository;
    }

    public List<AyudaPreguntaResponse> listarPreguntas() {
        return preguntaRepository.findByActivoTrueOrderByOrdenAsc()
                .stream()
                .map(this::toPreguntaResponse)
                .collect(Collectors.toList());
    }

    public List<AyudaCategoriaResponse> listarCategorias() {
        return categoriaRepository.findByActivoTrueOrderByOrdenAsc()
                .stream()
                .map(this::toCategoriaResponse)
                .collect(Collectors.toList());
    }

    private AyudaPreguntaResponse toPreguntaResponse(AyudaPregunta pregunta) {
        AyudaPreguntaResponse response = new AyudaPreguntaResponse();
        response.setId(pregunta.getId());
        response.setPregunta(pregunta.getPregunta());
        response.setRespuesta(pregunta.getRespuesta());
        response.setCategoria(pregunta.getCategoria());
        response.setOrden(pregunta.getOrden());
        return response;
    }

    private AyudaCategoriaResponse toCategoriaResponse(AyudaCategoria categoria) {
        AyudaCategoriaResponse response = new AyudaCategoriaResponse();
        response.setId(categoria.getId());
        response.setNombre(categoria.getNombre());
        response.setIcono(categoria.getIcono());
        response.setColor(categoria.getColor());
        response.setDescripcion(categoria.getDescripcion());
        response.setOrden(categoria.getOrden());
        return response;
    }
}
