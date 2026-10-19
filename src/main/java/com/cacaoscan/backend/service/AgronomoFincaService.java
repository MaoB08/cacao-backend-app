package com.cacaoscan.backend.service;

import com.cacaoscan.backend.dto.FincaDTOs.FincaResponse;
import com.cacaoscan.backend.dto.LoteDTOs;
import com.cacaoscan.backend.exception.ResourceNotFoundException;
import com.cacaoscan.backend.model.AgronomoFinca;
import com.cacaoscan.backend.model.Finca;
import com.cacaoscan.backend.model.Rol;
import com.cacaoscan.backend.model.Usuario;
import com.cacaoscan.backend.repository.AgronomoFincaRepository;
import com.cacaoscan.backend.repository.FincaRepository;
import com.cacaoscan.backend.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AgronomoFincaService {

    private final AgronomoFincaRepository agronomoFincaRepository;
    private final FincaRepository fincaRepository;
    private final UsuarioRepository usuarioRepository;

    public AgronomoFincaService(AgronomoFincaRepository agronomoFincaRepository,
                                FincaRepository fincaRepository,
                                UsuarioRepository usuarioRepository) {
        this.agronomoFincaRepository = agronomoFincaRepository;
        this.fincaRepository = fincaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Lista las fincas activas asignadas al agrónomo autenticado.
     */
    @Transactional(readOnly = true)
    public Page<FincaResponse> listarFincasAsignadas(String userEmail, String search, Pageable pageable) {
        Usuario agronomo = obtenerAgronomo(userEmail);

        Page<AgronomoFinca> asignaciones;
        if (search != null && !search.trim().isBlank()) {
            asignaciones = agronomoFincaRepository.findFincasAsignadasConBusqueda(agronomo, search.trim(), pageable);
        } else {
            asignaciones = agronomoFincaRepository.findFincasAsignadas(agronomo, pageable);
        }

        return asignaciones.map(af -> toFincaResponse(af.getFinca()));
    }

    /**
     * Obtiene detalle de una finca asignada, incluyendo lotes y propietario.
     */
    @Transactional(readOnly = true)
    public FincaDetalleAgronomoResponse obtenerDetalleFinca(UUID fincaId, String userEmail) {
        Usuario agronomo = obtenerAgronomo(userEmail);
        Finca finca = fincaRepository.findById(fincaId)
                .orElseThrow(() -> new ResourceNotFoundException("Finca no encontrada"));

        validarAsignacion(agronomo, finca);

        FincaDetalleAgronomoResponse response = new FincaDetalleAgronomoResponse();
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

        // Propietario
        Usuario propietario = finca.getUsuario();
        response.setAgricultorId(propietario.getId());
        response.setAgricultorNombre(propietario.getNombre());
        response.setAgricultorTelefono(propietario.getTelefono());

        // Lotes
        if (finca.getLotes() != null) {
            List<LoteDTOs.LoteResumenResponse> lotes = finca.getLotes().stream()
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
            response.setLotes(lotes);
        }

        return response;
    }

    /**
     * Verifica si el agrónomo tiene asignación activa con la finca.
     */
    @Transactional(readOnly = true)
    public boolean tieneAsignacion(Usuario agronomo, Finca finca) {
        return agronomoFincaRepository.existsByAgronomoAndFincaAndActivaTrue(agronomo, finca);
    }

    /**
     * Valida que el agrónomo tenga asignación activa con la finca; lanza excepción si no.
     */
    public void validarAsignacion(Usuario agronomo, Finca finca) {
        if (!tieneAsignacion(agronomo, finca)) {
            throw new IllegalArgumentException("No tienes asignación activa con esta finca");
        }
    }

    /**
     * Cuenta fincas activas asignadas al agrónomo.
     */
    @Transactional(readOnly = true)
    public long contarFincasAsignadas(Usuario agronomo) {
        return agronomoFincaRepository.countFincasActivasByAgronomo(agronomo);
    }

    // ======================== Helpers ========================

    public Usuario obtenerAgronomo(String userEmail) {
        Usuario usuario = usuarioRepository.findByIdentificador(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (usuario.getRol() != Rol.EXPERTO) {
            throw new IllegalArgumentException("Solo los agrónomos pueden acceder a este recurso");
        }
        return usuario;
    }

    private FincaResponse toFincaResponse(Finca finca) {
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

    // ======================== Inner DTO ========================

    /**
     * Detalle de finca para el agrónomo: incluye datos del agricultor propietario.
     */
    public static class FincaDetalleAgronomoResponse extends FincaResponse {
        private UUID agricultorId;
        private String agricultorNombre;
        private String agricultorTelefono;
        private List<LoteDTOs.LoteResumenResponse> lotes;

        public UUID getAgricultorId() { return agricultorId; }
        public void setAgricultorId(UUID agricultorId) { this.agricultorId = agricultorId; }

        public String getAgricultorNombre() { return agricultorNombre; }
        public void setAgricultorNombre(String agricultorNombre) { this.agricultorNombre = agricultorNombre; }

        public String getAgricultorTelefono() { return agricultorTelefono; }
        public void setAgricultorTelefono(String agricultorTelefono) { this.agricultorTelefono = agricultorTelefono; }

        public List<LoteDTOs.LoteResumenResponse> getLotes() { return lotes; }
        public void setLotes(List<LoteDTOs.LoteResumenResponse> lotes) { this.lotes = lotes; }
    }
}
