package com.cacaoscan.backend.service;

import com.cacaoscan.backend.dto.FincaDTOs.*;
import com.cacaoscan.backend.dto.LoteDTOs;
import com.cacaoscan.backend.exception.ResourceNotFoundException;
import com.cacaoscan.backend.model.Finca;
import com.cacaoscan.backend.model.Usuario;
import com.cacaoscan.backend.repository.FincaRepository;
import com.cacaoscan.backend.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FincaService {

    private final FincaRepository fincaRepository;
    private final UsuarioRepository usuarioRepository;

    public FincaService(FincaRepository fincaRepository, UsuarioRepository usuarioRepository) {
        this.fincaRepository = fincaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public FincaResponse crearFinca(CrearFincaRequest request, String userEmail) {
        Usuario usuario = usuarioRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (fincaRepository.existsByUsuarioAndNombreIgnoreCase(usuario, request.getNombre())) {
            throw new IllegalArgumentException("Ya existe una finca con ese nombre");
        }

        Finca finca = new Finca();
        finca.setUsuario(usuario);
        finca.setNombre(request.getNombre());
        finca.setDepartamento(request.getDepartamento() != null ? request.getDepartamento() : usuario.getDepartamento());
        finca.setMunicipio(request.getMunicipio() != null ? request.getMunicipio() : usuario.getMunicipio());
        finca.setVereda(request.getVereda());
        finca.setDescripcion(request.getDescripcion());

        Finca saved = fincaRepository.save(finca);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<FincaResponse> listarFincas(String userEmail) {
        Usuario usuario = usuarioRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        return fincaRepository.findByUsuarioAndActivaTrueOrderByCreatedAtDesc(usuario)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FincaDetalleResponse obtenerDetalle(UUID fincaId, String userEmail) {
        Finca finca = fincaRepository.findById(fincaId)
                .orElseThrow(() -> new ResourceNotFoundException("Finca no encontrada"));

        validarPropietario(finca, userEmail);

        FincaDetalleResponse response = new FincaDetalleResponse();
        response.setId(finca.getId());
        response.setNombre(finca.getNombre());
        response.setDepartamento(finca.getDepartamento());
        response.setMunicipio(finca.getMunicipio());
        response.setVereda(finca.getVereda());
        response.setAreaTotalHectareas(finca.getAreaTotalHectareas());
        response.setDescripcion(finca.getDescripcion());
        response.setActiva(finca.isActiva());
        response.setTotalLotes(finca.getLotes().size());
        response.setCreatedAt(finca.getCreatedAt());

        List<LoteDTOs.LoteResumenResponse> lotesResumen = finca.getLotes().stream()
                .map(lote -> {
                    LoteDTOs.LoteResumenResponse lr = new LoteDTOs.LoteResumenResponse();
                    lr.setId(lote.getId());
                    lr.setNombre(lote.getNombre());
                    lr.setVariedadCacao(lote.getVariedadCacao());
                    lr.setAreaHectareas(lote.getAreaHectareas());
                    lr.setColorHex(lote.getColorHex());
                    lr.setEstado(lote.getEstado().name());
                    lr.setCreatedAt(lote.getCreatedAt());
                    return lr;
                })
                .collect(Collectors.toList());

        response.setLotes(lotesResumen);
        return response;
    }

    // ======================== Mappers ========================

    private FincaResponse toResponse(Finca finca) {
        FincaResponse response = new FincaResponse();
        response.setId(finca.getId());
        response.setNombre(finca.getNombre());
        response.setDepartamento(finca.getDepartamento());
        response.setMunicipio(finca.getMunicipio());
        response.setVereda(finca.getVereda());
        response.setAreaTotalHectareas(finca.getAreaTotalHectareas());
        response.setDescripcion(finca.getDescripcion());
        response.setActiva(finca.isActiva());
        response.setTotalLotes(finca.getLotes() != null ? finca.getLotes().size() : 0);
        response.setCreatedAt(finca.getCreatedAt());
        return response;
    }

    private void validarPropietario(Finca finca, String userEmail) {
        if (!finca.getUsuario().getEmail().equals(userEmail)) {
            throw new IllegalArgumentException("No tienes permisos sobre esta finca");
        }
    }
}
